package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.embedding.HttpEmbeddingProvider;
import com.neviswealth.searchservice.embedding.MockEmbeddingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingProvider embeddingProvider(EmbeddingProperties properties, RestClient.Builder restClientBuilder) {
        EmbeddingProviderType providerType = EmbeddingProviderType.from(properties.getProvider());
        return switch (providerType) {
            case HTTP -> new HttpEmbeddingProvider(buildRestClient(properties, restClientBuilder));
            case MOCK -> new MockEmbeddingProvider(properties.getDimension());
        };
    }

    private RestClient buildRestClient(EmbeddingProperties properties, RestClient.Builder builder) {
        var httpProps = properties.getHttp();
        RestClient.Builder configured = builder.baseUrl(httpProps.getBaseUrl());
        if (StringUtils.hasText(httpProps.getApiToken())) {
            configured = configured.defaultHeader("Authorization", "Bearer " + httpProps.getApiToken());
        }
        return configured.build();
    }
}
