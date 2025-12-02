package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.embedding.EmbeddingErrorDecoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

public class EmbeddingFeignConfig {

    @Bean
    public RequestInterceptor embeddingAuthInterceptor(EmbeddingProperties properties) {
        return template -> {
            String token = properties.getHttp().getApiToken();
            if (token != null && !token.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        };
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new EmbeddingErrorDecoder();
    }

}
