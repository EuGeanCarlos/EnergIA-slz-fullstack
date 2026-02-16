package br.com.energia.energiaslz.config;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper tolerante para ler "quase JSON" vindo do Gemini:
     * - vírgula sobrando
     * - aspas simples
     * - caracteres de controle sem escape
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .build();
    }
}
