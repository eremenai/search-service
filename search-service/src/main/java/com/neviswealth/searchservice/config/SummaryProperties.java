package com.neviswealth.searchservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "summary")
public class SummaryProperties {

    @NotNull
    private ProviderType provider = ProviderType.MOCK;

    private String promptEngineering;

    @NestedConfigurationProperty
    private final Http http = new Http();

    public ProviderType getProvider() {
        return provider;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public Http getHttp() {
        return http;
    }

    public String getPromptEngineering() {
        return promptEngineering;
    }

    public void setPromptEngineering(String promptEngineering) {
        this.promptEngineering = promptEngineering;
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
