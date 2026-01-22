package br.com.energia.energiaslz.dto.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatResponseDTO {

    private String resposta;
    private List<RecomendacaoDTO> recomendacoes = new ArrayList<>();
    private ImpactoDTO impacto;
    private RelatorioResumoDTO relatorio;

    public ChatResponseDTO() {}

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public List<RecomendacaoDTO> getRecomendacoes() { return recomendacoes; }
    public void setRecomendacoes(List<RecomendacaoDTO> recomendacoes) { this.recomendacoes = recomendacoes; }

    public ImpactoDTO getImpacto() { return impacto; }
    public void setImpacto(ImpactoDTO impacto) { this.impacto = impacto; }

    public RelatorioResumoDTO getRelatorio() { return relatorio; }
    public void setRelatorio(RelatorioResumoDTO relatorio) { this.relatorio = relatorio; }

    public static class RecomendacaoDTO {
        private String titulo;
        private String descricao;
        private String impacto;

        public RecomendacaoDTO() {}

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }

        public String getImpacto() { return impacto; }
        public void setImpacto(String impacto) { this.impacto = impacto; }
    }

    public static class ImpactoDTO {
        private Integer economiaPercentual;
        private Double economiaMensalReais;
        private Double economiaAnualReais;
        private Double co2EvitadoKgMes;

        public ImpactoDTO() {}

        public Integer getEconomiaPercentual() { return economiaPercentual; }
        public void setEconomiaPercentual(Integer economiaPercentual) { this.economiaPercentual = economiaPercentual; }

        public Double getEconomiaMensalReais() { return economiaMensalReais; }
        public void setEconomiaMensalReais(Double economiaMensalReais) { this.economiaMensalReais = economiaMensalReais; }

        public Double getEconomiaAnualReais() { return economiaAnualReais; }
        public void setEconomiaAnualReais(Double economiaAnualReais) { this.economiaAnualReais = economiaAnualReais; }

        public Double getCo2EvitadoKgMes() { return co2EvitadoKgMes; }
        public void setCo2EvitadoKgMes(Double co2EvitadoKgMes) { this.co2EvitadoKgMes = co2EvitadoKgMes; }
    }

    public static class RelatorioResumoDTO {
        private Double consumoMensalKwh;
        private Double custoEstimado;

        public RelatorioResumoDTO() {}

        public RelatorioResumoDTO(Double consumoMensalKwh, Double custoEstimado) {
            this.consumoMensalKwh = consumoMensalKwh;
            this.custoEstimado = custoEstimado;
        }

        public Double getConsumoMensalKwh() { return consumoMensalKwh; }
        public void setConsumoMensalKwh(Double consumoMensalKwh) { this.consumoMensalKwh = consumoMensalKwh; }

        public Double getCustoEstimado() { return custoEstimado; }
        public void setCustoEstimado(Double custoEstimado) { this.custoEstimado = custoEstimado; }
    }
}
