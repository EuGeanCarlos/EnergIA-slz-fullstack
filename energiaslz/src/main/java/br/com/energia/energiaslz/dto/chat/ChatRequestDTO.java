package br.com.energia.energiaslz.dto.chat;

public class ChatRequestDTO {
    private String usuarioId;
    private String mensagem;

    public ChatRequestDTO() {}

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
