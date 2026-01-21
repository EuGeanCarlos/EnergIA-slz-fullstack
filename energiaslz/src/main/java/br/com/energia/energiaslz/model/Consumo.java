package br.com.energia.energiaslz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "consumos")
public class Consumo {

    @Id
    private String id;

    @NotBlank(message = "Usuário é obrigatório")
    private String usuarioId;

    @NotBlank(message = "Nome do aparelho é obrigatório")
    private String nomeAparelho;

    // ✅ NOVO: setor (muito útil pra IA)
    private String setor;

    // ✅ NOVO: observação (opcional)
    private String observacao;

    @NotNull(message = "Potência é obrigatória")
    @Positive(message = "Potência deve ser maior que zero")
    private Integer potencia;

    @NotNull(message = "Horas de uso são obrigatórias")
    @Positive(message = "Horas de uso devem ser maiores que zero")
    private Double horasUso;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    private LocalDateTime dataRegistro;

    public Consumo() {
        this.dataRegistro = LocalDateTime.now();
    }

    public String getId() { return id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getNomeAparelho() { return nomeAparelho; }
    public void setNomeAparelho(String nomeAparelho) { this.nomeAparelho = nomeAparelho; }

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Integer getPotencia() { return potencia; }
    public void setPotencia(Integer potencia) { this.potencia = potencia; }

    public Double getHorasUso() { return horasUso; }
    public void setHorasUso(Double horasUso) { this.horasUso = horasUso; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
}
