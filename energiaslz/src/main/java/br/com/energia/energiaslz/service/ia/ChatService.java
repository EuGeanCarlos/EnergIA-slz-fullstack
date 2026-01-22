package br.com.energia.energiaslz.service.ia;

import br.com.energia.energiaslz.dto.RelatorioDTO;
import br.com.energia.energiaslz.dto.chat.ChatRequestDTO;
import br.com.energia.energiaslz.dto.chat.ChatResponseDTO;
import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.model.Usuario;
import br.com.energia.energiaslz.service.ConsumoService;
import br.com.energia.energiaslz.service.RelatorioService;
import br.com.energia.energiaslz.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    private final UsuarioService usuarioService;
    private final ConsumoService consumoService;
    private final RelatorioService relatorioService;
    private final OllamaClient ollamaClient; // ✅ agora resolve no mesmo package
    private final ObjectMapper objectMapper;

    @Value("${ai.provider:ollama}")
    private String aiProvider;

    public ChatService(UsuarioService usuarioService,
                       ConsumoService consumoService,
                       RelatorioService relatorioService,
                       OllamaClient ollamaClient,
                       ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.consumoService = consumoService;
        this.relatorioService = relatorioService;
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponseDTO responder(ChatRequestDTO req) {
        if (req == null || req.getUsuarioId() == null || req.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("usuarioId é obrigatório.");
        }

        String usuarioId = req.getUsuarioId();
        String mensagem = (req.getMensagem() == null || req.getMensagem().isBlank())
                ? "Gere um diagnóstico e recomendações para reduzir custo de energia."
                : req.getMensagem();

        Usuario empresa = usuarioService.buscarPorId(usuarioId);
        List<Consumo> consumos = consumoService.listarPorUsuario(usuarioId);
        RelatorioDTO relatorio = relatorioService.gerarRelatorioPorUsuario(usuarioId);

        double consumoKwh = safe(relatorio.getConsumoMensalKwh());
        double custo = safe(relatorio.getCustoEstimado());
        double co2KgMes = consumoKwh * 0.084;

        // fallback se IA estiver desligada
        if (!"ollama".equalsIgnoreCase(aiProvider)) {
            return respostaDeterministica(consumoKwh, custo, co2KgMes);
        }

        String system = systemPrompt();
        String user = userPrompt(empresa, consumos, consumoKwh, custo, co2KgMes, mensagem);

        List<Map<String, String>> messages = List.of(
                msg("system", system),
                msg("user", user)
        );

        String conteudo = ollamaClient.chat(messages);

        // tenta parsear JSON
        try {
            ChatResponseDTO parsed = objectMapper.readValue(conteudo, ChatResponseDTO.class);

            // força dados determinísticos do backend
            parsed.setRelatorio(new ChatResponseDTO.RelatorioResumoDTO(consumoKwh, custo));

            // se impacto vier nulo, cria vazio
            if (parsed.getImpacto() == null) {
                parsed.setImpacto(impactoZero());
            }

            if (parsed.getRecomendacoes() == null) parsed.setRecomendacoes(new ArrayList<>());

            return parsed;
        } catch (Exception e) {
            // fallback: devolve texto cru + números
            ChatResponseDTO resp = new ChatResponseDTO();
            resp.setResposta(conteudo);
            resp.setRelatorio(new ChatResponseDTO.RelatorioResumoDTO(consumoKwh, custo));
            resp.setImpacto(impactoZero());
            resp.setRecomendacoes(Collections.emptyList());
            return resp;
        }
    }

    private ChatResponseDTO respostaDeterministica(double consumoKwh, double custo, double co2KgMes) {
        ChatResponseDTO resp = new ChatResponseDTO();
        resp.setResposta("IA indisponível no momento. Aqui está um diagnóstico básico com base nos seus dados.");
        resp.setRelatorio(new ChatResponseDTO.RelatorioResumoDTO(consumoKwh, custo));
        resp.setImpacto(impactoZero());

        List<ChatResponseDTO.RecomendacaoDTO> recs = new ArrayList<>();
        recs.add(rec("Revisar horários de uso", "Garanta que equipamentos não fiquem ligados fora do expediente.", "Impacto: redução de desperdício"));
        recs.add(rec("Stand-by e desligamento", "Evite consumo invisível (TV, roteadores, micro-ondas com relógio).", "Impacto: economia constante"));
        recs.add(rec("Manutenção preventiva", "Filtros, vedação e limpeza melhoram eficiência (AC/freezer).", "Impacto: melhora desempenho"));
        recs.add(rec("Iluminação eficiente", "Troque para LED e priorize luz natural.", "Impacto: economia no uso contínuo"));
        recs.add(rec("Rotina de controle", "Crie checklist diário de desligamento por setor.", "Impacto: organização operacional"));
        resp.setRecomendacoes(recs);

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

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private String systemPrompt() {
        return """
                Você é um consultor de eficiência energética especializado em microempresas.
                Regras:
                - NÃO invente números. Use apenas os dados fornecidos (kWh, R$, CO2).
                - Responda SOMENTE em JSON válido (sem markdown e sem texto fora do JSON).
                - Gere recomendações práticas e priorizadas (5 itens).
                                
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

        sb.append("\nPEDIDO:\n").append(mensagem).append("\n");

        sb.append("\nTAREFA:\n");
        sb.append("Gere diagnóstico curto + 5 recomendações priorizadas.\n");
        sb.append("Se estimar impacto, seja conservador e derive de custo/consumo fornecidos. Se não der, use 0.\n");
        sb.append("No campo relatorio, repita consumoMensalKwh e custoEstimado exatamente.\n");

        return sb.toString();
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private double safe(Double v) {
        return v == null ? 0.0 : v;
    }
}
