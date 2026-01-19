package br.com.energia.energiaslz.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "consumos")
public class Consumo {

    @Id
    private String id;

    private String nomeAparelho;
    private Integer potencia;
    private Double horasUso;
    private Integer quantidade;
    private LocalDateTime dataRegistro;

    public Consumo() {
        this.dataRegistro = LocalDateTime.now();
    }

    // getters e setters

    public String getId() {
        return id;
    }

    public String getNomeAparelho() {
        return nomeAparelho;
    }

    public void setNomeAparelho(String nomeAparelho) {
        this.nomeAparelho = nomeAparelho;
    }

    public Integer getPotencia() {
        return potencia;
    }

    public void setPotencia(Integer potencia) {
        this.potencia = potencia;
    }

    public Double getHorasUso() {
        return horasUso;
    }

    public void setHorasUso(Double horasUso) {
        this.horasUso = horasUso;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}
