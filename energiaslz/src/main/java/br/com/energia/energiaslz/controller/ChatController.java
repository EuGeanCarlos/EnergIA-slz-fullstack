package br.com.energia.energiaslz.controller;

import br.com.energia.energiaslz.dto.chat.ChatRequestDTO;
import br.com.energia.energiaslz.dto.chat.ChatResponseDTO;
import br.com.energia.energiaslz.service.ia.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO resp = chatService.responder(request);
        return ResponseEntity.ok(resp);
    }
    @GetMapping("/chat")
    public ResponseEntity<?> chatStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Use POST /api/chat com JSON {usuarioId, mensagem}"
        ));
    }

}
