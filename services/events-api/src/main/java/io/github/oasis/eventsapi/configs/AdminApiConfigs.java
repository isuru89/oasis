package io.github.oasis.eventsapi.configs;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.configs.OasisConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AdminApiConfigs {

    @Bean
    public WebClient createAdminApiWebClient(OasisConfigs configs) {
        var adminApiBaseUrl = configs.get("oasis.adminApi.baseUrl", "");
        var apiKey = configs.get("oasis.adminApi.apiKey", "");
        var apiSecretKey = configs.get("oasis.adminApi.secretKey", "");


        return WebClient.builder()
                .baseUrl(adminApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .defaultHeader("X-APP-ID", apiKey)
                .defaultHeader("X-APP-KEY", apiSecretKey)
                .build();
    }

    @Bean
    public ObjectMapper newJacksonMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }
}
