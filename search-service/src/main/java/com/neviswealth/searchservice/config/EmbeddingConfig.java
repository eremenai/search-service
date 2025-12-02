package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.embedding.EmbeddingClient;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.embedding.HttpEmbeddingProvider;
import com.neviswealth.searchservice.embedding.MockEmbeddingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingProvider embeddingProvider(EmbeddingProperties properties, EmbeddingClient embeddingClient) {
        return switch (properties.getProvider()) {
            case HTTP -> new HttpEmbeddingProvider(embeddingClient);
            case MOCK -> new MockEmbeddingProvider(properties.getDimension());
        };
    }
}
