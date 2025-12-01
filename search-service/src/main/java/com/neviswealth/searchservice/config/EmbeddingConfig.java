package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.embedding.HttpEmbeddingProvider;
import com.neviswealth.searchservice.embedding.MockEmbeddingProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class EmbeddingConfig {

    @Bean
    @Qualifier("embeddingRestTemplate")
    public RestTemplate externalRestTemplate(RestTemplateBuilder builder, EmbeddingProperties properties) {
        return builder
                .rootUri(properties.getHttp().getBaseUrl())
                .connectTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(5))
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(properties.getHttp().getApiToken());
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public EmbeddingProvider embeddingProvider(EmbeddingProperties properties, @Qualifier("embeddingRestTemplate") RestTemplate restTemplate) {
        EmbeddingProviderType providerType = EmbeddingProviderType.from(properties.getProvider());
        return switch (providerType) {
            case HTTP -> new HttpEmbeddingProvider(restTemplate);
            case MOCK -> new MockEmbeddingProvider(properties.getDimension());
        };
    }
}
