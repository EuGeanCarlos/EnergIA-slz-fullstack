package br.com.energia.energiaslz.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class StatusController {

    // Endpoint simples (texto) — útil pra testar no navegador
    @GetMapping("/status")
    public String statusTexto() {
        return "API está funcionando! - " + new Date();
    }

    // Endpoint que o FRONT deve usar: /api/status (JSON)
    @GetMapping(value = "/api/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> statusApi() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mensagem", "API está funcionando!");
        res.put("message", "API está funcionando!"); // redundância de compatibilidade
        res.put("timestamp", new Date().toString());
        return ResponseEntity.ok(res);
    }
}
