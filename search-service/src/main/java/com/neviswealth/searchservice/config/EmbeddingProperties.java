package com.neviswealth.searchservice.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties {

    @NotBlank
    private String provider = "mock";

    @Min(1)
    private int dimension = 768;

    @NestedConfigurationProperty
    private final Http http = new Http();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public Http getHttp() {
        return http;
    }

    @Validated
    public static class Http {
        @NotBlank
        private String baseUrl = "http://localhost:8000";

        private String apiToken = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }
    }
}
