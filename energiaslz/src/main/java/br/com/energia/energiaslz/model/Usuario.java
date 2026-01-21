package br.com.energia.energiaslz.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    // Responsável
    private String nome;
    private String email;
    private String telefone;

    // Empresa
    private String endereco;
    private String nomeEmpresa;
    private String segmento;
    private String horarioFuncionamento;

    // Operação
    private Double tarifa;
    private Integer numResidentes; // (no front virou “colaboradores”, mas mantenho por compatibilidade)
    private Double faturamentoMensalEstimado;

    public Usuario() {}

    public String getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Double getTarifa() { return tarifa; }
    public void setTarifa(Double tarifa) { this.tarifa = tarifa; }

    public Integer getNumResidentes() { return numResidentes; }
    public void setNumResidentes(Integer numResidentes) { this.numResidentes = numResidentes; }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public String getHorarioFuncionamento() { return horarioFuncionamento; }
    public void setHorarioFuncionamento(String horarioFuncionamento) { this.horarioFuncionamento = horarioFuncionamento; }

    public Double getFaturamentoMensalEstimado() { return faturamentoMensalEstimado; }
    public void setFaturamentoMensalEstimado(Double faturamentoMensalEstimado) { this.faturamentoMensalEstimado = faturamentoMensalEstimado; }
}
