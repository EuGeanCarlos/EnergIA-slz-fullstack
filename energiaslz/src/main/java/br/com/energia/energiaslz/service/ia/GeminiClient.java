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

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY não configurada. Defina a variável de ambiente GEMINI_API_KEY.");
        }

        // endpoint: /{version}/models/{model}:generateContent
        String url = baseUrl + "/" + apiVersion + "/models/" + model + ":generateContent";

        // body: contents[].parts[].text
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        byte[] rawBytes = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey) //
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
                    return parts.get(0).path("text").asText(raw);
                }
            }
            return raw;
        } catch (Exception e) {
            return raw;
        }
    }

    public String generateJson(String system, String user) {
    }
}
