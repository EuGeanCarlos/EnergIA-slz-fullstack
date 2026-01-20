package br.com.energia.energiaslz.dto;

public class RelatorioDTO {

    private Double consumoMensalKwh;
    private Double custoEstimado;

    public RelatorioDTO(Double consumoMensalKwh, Double custoEstimado) {
        this.consumoMensalKwh = consumoMensalKwh;
        this.custoEstimado = custoEstimado;
    }

    public Double getConsumoMensalKwh() {
        return consumoMensalKwh;
    }

    public Double getCustoEstimado() {
        return custoEstimado;
    }
}
