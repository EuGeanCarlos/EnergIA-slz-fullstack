package br.com.energia.energiaslz.service.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${ai.gemini.api-version:v1beta}")
    private String apiVersion;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    public GeminiClient(ObjectMapper objectMapper,
                        @Value("${ai.gemini.timeout-seconds:60}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(timeoutSeconds * 1000);
                    setReadTimeout(timeoutSeconds * 1000);
                }})
                .build();
    }

    /**
     * Gera texto bruto do Gemini (normalmente candidates[0].content.parts[0].text).
     */
    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY não configurada. Defina a variável de ambiente GEMINI_API_KEY.");
        }

        String url = baseUrl + "/" + apiVersion + "/models/" + model + ":generateContent";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )
        ));

        // Força saída JSON quando suportado (ajuda mas não garante 100%)
        body.put("generationConfig", Map.of(
                "temperature", 0.2,
                "maxOutputTokens", 1024,
                "responseMimeType", "application/json"
        ));

        byte[] rawBytes = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey)
                .body(body)
                .retrieve()
                .body(byte[].class);

        String raw = rawBytes == null ? "" : new String(rawBytes, StandardCharsets.UTF_8);

        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode textNode = parts.get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText("");
                    }
                }
            }
            return raw;
        } catch (Exception e) {
            return raw;
        }
    }

    /**
     * Envia system+user num único prompt e retorna SOMENTE JSON (sanitizado).
     */
    public String generateJson(String system, String user) {
        String prompt =
                "INSTRUÇÕES DO SISTEMA:\n" + safe(system) + "\n\n" +
                        "CONTEXTO DO USUÁRIO:\n" + safe(user) + "\n\n" +
                        "REGRA ABSOLUTA:\n" +
                        "- Responda SOMENTE com um JSON válido.\n" +
                        "- Não use markdown.\n" +
                        "- Não escreva texto antes ou depois do JSON.\n";

        String raw = generateText(prompt);
        return sanitizeToPureJson(raw);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Remove markdown e tenta extrair apenas o PRIMEIRO objeto JSON completo { ... }.
     * Faz balanceamento de chaves para não depender de lastIndexOf('}').
     */
    private String sanitizeToPureJson(String raw) {
        if (raw == null) return "";

        String s = raw.trim();

        // Remove cercas ```json ... ```
        if (s.startsWith("```")) {
            int firstNewLine = s.indexOf('\n');
            if (firstNewLine > -1) s = s.substring(firstNewLine + 1).trim();
            int lastFence = s.lastIndexOf("```");
            if (lastFence > -1) s = s.substring(0, lastFence).trim();
        }

        // Tenta extrair o primeiro JSON bem-formado por balanceamento de chaves
        int start = s.indexOf('{');
        if (start < 0) return s;

        int depth = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            } else {
                if (c == '"') {
                    inString = true;
                    continue;
                }
                if (c == '{') depth++;
                if (c == '}') depth--;

                if (depth == 0) {
                    // fecha o primeiro objeto
                    return s.substring(start, i + 1).trim();
                }
            }
        }

        // Se não fechou, devolve como veio (ChatService decide fallback)
        return s.substring(start).trim();
    }
}
