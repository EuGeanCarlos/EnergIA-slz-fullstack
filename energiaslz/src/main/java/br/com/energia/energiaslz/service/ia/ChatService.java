package br.com.energia.energiaslz.service.ia;

import br.com.energia.energiaslz.dto.RelatorioDTO;
import br.com.energia.energiaslz.dto.chat.ChatRequestDTO;
import br.com.energia.energiaslz.dto.chat.ChatResponseDTO;
import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.model.Usuario;
import br.com.energia.energiaslz.service.ConsumoService;
import br.com.energia.energiaslz.service.RelatorioService;
import br.com.energia.energiaslz.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final UsuarioService usuarioService;
    private final ConsumoService consumoService;
    private final RelatorioService relatorioService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    public ChatService(UsuarioService usuarioService,
                       ConsumoService consumoService,
                       RelatorioService relatorioService,
                       GeminiClient geminiClient,
                       ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.consumoService = consumoService;
        this.relatorioService = relatorioService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponseDTO responder(ChatRequestDTO req) {
        if (req == null || req.getUsuarioId() == null || req.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("usuarioId é obrigatório.");
        }

        String usuarioId = req.getUsuarioId().trim();
        String mensagem = (req.getMensagem() == null || req.getMensagem().isBlank())
                ? "Gere um diagnóstico e 5 recomendações priorizadas para reduzir custo de energia."
                : req.getMensagem().trim();

        Usuario empresa = usuarioService.buscarPorId(usuarioId);
        List<Consumo> consumos = consumoService.listarPorUsuario(usuarioId);
        RelatorioDTO relatorio = relatorioService.gerarRelatorioPorUsuario(usuarioId);

        double consumoKwh = safe(relatorio.getConsumoMensalKwh());
        double custo = safe(relatorio.getCustoEstimado());
        double co2KgMes = consumoKwh * 0.084;

        // IA desligada? devolve determinístico
        if (!"gemini".equalsIgnoreCase(aiProvider)) {
            return respostaDeterministica(consumoKwh, custo, co2KgMes);
        }

        String system = systemPrompt();
        String user = userPrompt(empresa, consumos, consumoKwh, custo, co2KgMes, mensagem);

        String conteudo = geminiClient.generateJson(system, user);

        // 1) tenta parse direto para DTO
        ChatResponseDTO resp = tryParseDto(conteudo);

        // 1.1) se veio JSON inteiro dentro do campo "resposta", desembrulha
        resp = unwrapIfResponseContainsJson(resp);

        // 2) se falhar, tenta extrair com JsonNode (mais tolerante)
        if (resp == null) {
            resp = tryParseLenient(conteudo);
            resp = unwrapIfResponseContainsJson(resp);
        }

        // 3) se ainda falhar, fallback total determinístico
        if (resp == null) {
            resp = respostaDeterministica(consumoKwh, custo, co2KgMes);
            resp.setResposta(
                    "Não foi possível interpretar a resposta da IA. Exibindo recomendações padrão.\n\nResposta bruta:\n" + conteudo
            );
        }

        // Força números determinísticos do backend (regra do projeto)
        resp.setRelatorio(new ChatResponseDTO.RelatorioResumoDTO(consumoKwh, custo));

        // Normaliza impacto
        if (resp.getImpacto() == null) {
            resp.setImpacto(impactoZero());
        } else {
            if (resp.getImpacto().getEconomiaPercentual() == null) resp.getImpacto().setEconomiaPercentual(0);
            if (resp.getImpacto().getEconomiaMensalReais() == null) resp.getImpacto().setEconomiaMensalReais(0.0);
            if (resp.getImpacto().getEconomiaAnualReais() == null) resp.getImpacto().setEconomiaAnualReais(0.0);
            if (resp.getImpacto().getCo2EvitadoKgMes() == null) resp.getImpacto().setCo2EvitadoKgMes(0.0);
        }

        // Normaliza recomendações e garante 5 itens
        resp.setRecomendacoes(normalizeAndEnsureFive(resp.getRecomendacoes()));

        // Resposta nunca vazia
        if (resp.getResposta() == null || resp.getResposta().isBlank()) {
            resp.setResposta("Diagnóstico gerado com base nos seus dados de consumo e equipamentos.");
        }

        return resp;
    }

    private ChatResponseDTO unwrapIfResponseContainsJson(ChatResponseDTO resp) {
        if (resp == null) return null;
        if (resp.getResposta() == null) return resp;

        String r = resp.getResposta().trim();

        // Caso clássico: o modelo colocou o JSON inteiro como string dentro de "resposta"
        if (r.startsWith("{") && r.endsWith("}")) {
            try {
                ChatResponseDTO unwrapped = objectMapper.readValue(r, ChatResponseDTO.class);
                return unwrapped != null ? unwrapped : resp;
            } catch (Exception ignored) {
                return resp;
            }
        }
        return resp;
    }

    private ChatResponseDTO tryParseDto(String json) {
        try {
            return objectMapper.readValue(json, ChatResponseDTO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Parse tolerante:
     * - Lê como JsonNode
     * - Extrai campos mesmo que o formato venha levemente diferente
     */
    private ChatResponseDTO tryParseLenient(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || root.isMissingNode() || root.isNull()) return null;

            ChatResponseDTO resp = new ChatResponseDTO();

            // resposta (string)
            resp.setResposta(asTextOrNull(root.get("resposta")));

            // impacto (objeto)
            JsonNode impacto = root.get("impacto");
            if (impacto != null && impacto.isObject()) {
                ChatResponseDTO.ImpactoDTO imp = new ChatResponseDTO.ImpactoDTO();
                imp.setEconomiaPercentual(asIntOrNull(impacto.get("economiaPercentual")));
                imp.setEconomiaMensalReais(asDoubleOrNull(impacto.get("economiaMensalReais")));
                imp.setEconomiaAnualReais(asDoubleOrNull(impacto.get("economiaAnualReais")));
                imp.setCo2EvitadoKgMes(asDoubleOrNull(impacto.get("co2EvitadoKgMes")));
                resp.setImpacto(imp);
            }

            // recomendacoes: pode vir como array OU objeto (erro comum)
            List<ChatResponseDTO.RecomendacaoDTO> recs = new ArrayList<>();
            JsonNode recomendacoes = root.get("recomendacoes");
            if (recomendacoes != null) {
                if (recomendacoes.isArray()) {
                    for (JsonNode item : recomendacoes) {
                        ChatResponseDTO.RecomendacaoDTO r = new ChatResponseDTO.RecomendacaoDTO();
                        r.setTitulo(asTextOrNull(item.get("titulo")));
                        r.setDescricao(asTextOrNull(item.get("descricao")));
                        r.setImpacto(asTextOrNull(item.get("impacto")));
                        if (isNotBlank(r.getTitulo()) || isNotBlank(r.getDescricao()) || isNotBlank(r.getImpacto())) {
                            recs.add(r);
                        }
                    }
                } else if (recomendacoes.isObject()) {
                    ChatResponseDTO.RecomendacaoDTO r = new ChatResponseDTO.RecomendacaoDTO();
                    r.setTitulo(asTextOrNull(recomendacoes.get("titulo")));
                    r.setDescricao(asTextOrNull(recomendacoes.get("descricao")));
                    r.setImpacto(asTextOrNull(recomendacoes.get("impacto")));
                    if (isNotBlank(r.getTitulo()) || isNotBlank(r.getDescricao()) || isNotBlank(r.getImpacto())) {
                        recs.add(r);
                    }
                }
            }
            resp.setRecomendacoes(recs);

            return resp;
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<ChatResponseDTO.RecomendacaoDTO> normalizeAndEnsureFive(List<ChatResponseDTO.RecomendacaoDTO> recs) {
        List<ChatResponseDTO.RecomendacaoDTO> out = new ArrayList<>();

        if (recs != null) {
            for (ChatResponseDTO.RecomendacaoDTO r : recs) {
                if (r == null) continue;
                if (!isNotBlank(r.getTitulo()) && !isNotBlank(r.getDescricao()) && !isNotBlank(r.getImpacto())) continue;

                if (r.getTitulo() == null) r.setTitulo("");
                if (r.getDescricao() == null) r.setDescricao("");
                if (r.getImpacto() == null) r.setImpacto("");

                out.add(r);
            }
        }

        // completa até 5 com determinísticas (pra nunca ficar feio no front)
        List<ChatResponseDTO.RecomendacaoDTO> base = recomendacoesBase();
        int i = 0;
        while (out.size() < 5 && i < base.size()) {
            out.add(base.get(i));
            i++;
        }

        // corta se vier mais de 5
        if (out.size() > 5) return out.subList(0, 5);

        return out;
    }

    private List<ChatResponseDTO.RecomendacaoDTO> recomendacoesBase() {
        List<ChatResponseDTO.RecomendacaoDTO> recs = new ArrayList<>();
        recs.add(rec("Revisar horários de uso",
                "Garanta que equipamentos não fiquem ligados fora do expediente e evite uso desnecessário em horários de pico.",
                "Redução de desperdício e controle de rotina"));
        recs.add(rec("Eliminar stand-by",
                "Desligue da tomada ou use régua com botão para TV, roteador, carregadores e aparelhos ociosos.",
                "Economia constante (consumo invisível)"));
        recs.add(rec("Manutenção preventiva",
                "Limpe filtros e ventilação, verifique vedação de geladeira/freezer e evite sobrecarga de tomadas.",
                "Melhora eficiência e reduz perdas"));
        recs.add(rec("Iluminação eficiente",
                "Troque lâmpadas por LED e aproveite luz natural; setorização ajuda a não iluminar áreas vazias.",
                "Economia no uso contínuo"));
        recs.add(rec("Checklist diário",
                "Crie uma rotina de fechamento: desligar equipamentos, luzes e estabilizadores ao final do expediente.",
                "Padronização e redução de desperdício"));
        return recs;
    }

    private ChatResponseDTO respostaDeterministica(double consumoKwh, double custo, double co2KgMes) {
        ChatResponseDTO resp = new ChatResponseDTO();
        resp.setResposta("Aqui está um diagnóstico básico com base nos seus dados de consumo e equipamentos.");
        resp.setRelatorio(new ChatResponseDTO.RelatorioResumoDTO(consumoKwh, custo));
        resp.setImpacto(impactoZero());
        resp.setRecomendacoes(recomendacoesBase());
        return resp;
    }

    private ChatResponseDTO.ImpactoDTO impactoZero() {
        ChatResponseDTO.ImpactoDTO imp = new ChatResponseDTO.ImpactoDTO();
        imp.setEconomiaPercentual(0);
        imp.setEconomiaMensalReais(0.0);
        imp.setEconomiaAnualReais(0.0);
        imp.setCo2EvitadoKgMes(0.0);
        return imp;
    }

    private ChatResponseDTO.RecomendacaoDTO rec(String t, String d, String i) {
        ChatResponseDTO.RecomendacaoDTO r = new ChatResponseDTO.RecomendacaoDTO();
        r.setTitulo(t);
        r.setDescricao(d);
        r.setImpacto(i);
        return r;
    }

    private String systemPrompt() {
        return """
                Você é um consultor de eficiência energética especializado em microempresas.
                Regras obrigatórias:
                - NÃO invente números. Use apenas os números fornecidos (kWh, R$, CO2).
                - Responda SOMENTE em JSON válido (sem markdown e sem texto fora do JSON).
                - Gere recomendações práticas e priorizadas (exatamente 5 itens).
                - Se não for possível estimar impacto, use 0.
                - NÃO coloque JSON dentro de string.

                Retorne exatamente este formato:
                {
                  "resposta": "texto curto e direto",
                  "recomendacoes": [
                    { "titulo": "...", "descricao": "...", "impacto": "..." }
                  ],
                  "impacto": {
                    "economiaPercentual": 0,
                    "economiaMensalReais": 0.0,
                    "economiaAnualReais": 0.0,
                    "co2EvitadoKgMes": 0.0
                  },
                  "relatorio": {
                    "consumoMensalKwh": 0.0,
                    "custoEstimado": 0.0
                  }
                }
                """;
    }

    private String userPrompt(Usuario empresa, List<Consumo> consumos,
                              double consumoKwh, double custo, double co2KgMes,
                              String mensagem) {

        StringBuilder sb = new StringBuilder();

        sb.append("DADOS DA EMPRESA:\n");
        sb.append("- Responsável: ").append(nvl(empresa.getNome())).append("\n");
        sb.append("- Email: ").append(nvl(empresa.getEmail())).append("\n");
        sb.append("- Telefone: ").append(nvl(empresa.getTelefone())).append("\n");
        sb.append("- Endereço: ").append(nvl(empresa.getEndereco())).append("\n");
        sb.append("- Colaboradores: ").append(empresa.getNumResidentes() != null ? empresa.getNumResidentes() : 0).append("\n");
        sb.append("- Tarifa (R$/kWh): ").append(empresa.getTarifa() != null ? empresa.getTarifa() : 0.75).append("\n\n");

        sb.append("NÚMEROS DETERMINÍSTICOS (usar exatamente):\n");
        sb.append("- Consumo mensal (kWh): ").append(consumoKwh).append("\n");
        sb.append("- Custo mensal (R$): ").append(custo).append("\n");
        sb.append("- CO2 estimado (kg/mês): ").append(co2KgMes).append("\n\n");

        sb.append("EQUIPAMENTOS:\n");
        if (consumos == null || consumos.isEmpty()) {
            sb.append("- (nenhum)\n");
        } else {
            for (Consumo c : consumos) {
                sb.append("- ").append(nvl(c.getNomeAparelho()))
                        .append(" | W=").append(c.getPotencia() != null ? c.getPotencia() : 0)
                        .append(" | h/dia=").append(c.getHorasUso() != null ? c.getHorasUso() : 0)
                        .append(" | qtd=").append(c.getQuantidade() != null ? c.getQuantidade() : 0)
                        .append("\n");
            }
        }

        sb.append("\nPEDIDO DO USUÁRIO:\n").append(mensagem).append("\n\n");

        sb.append("TAREFA:\n");
        sb.append("Gere diagnóstico curto + 5 recomendações priorizadas.\n");
        sb.append("No campo 'recomendacoes', gere EXATAMENTE 5 itens.\n");
        sb.append("No campo relatorio, repita consumoMensalKwh e custoEstimado exatamente.\n");
        sb.append("Não coloque JSON dentro de string.\n");

        return sb.toString();
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private double safe(Double v) {
        return v == null ? 0.0 : v;
    }

    private String asTextOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        String s = n.asText(null);
        return (s == null) ? null : s.trim();
    }

    private Integer asIntOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        if (n.isInt() || n.isLong()) return n.asInt();
        if (n.isTextual()) {
            try { return Integer.parseInt(n.asText().trim()); } catch (Exception ignored) {}
        }
        if (n.isNumber()) return n.asInt();
        return null;
    }

    private Double asDoubleOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        if (n.isNumber()) return n.asDouble();
        if (n.isTextual()) {
            try {
                String s = n.asText().trim().replace(",", ".");
                return Double.parseDouble(s);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
