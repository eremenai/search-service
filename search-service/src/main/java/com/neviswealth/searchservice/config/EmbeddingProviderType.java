package com.neviswealth.searchservice.config;

public enum EmbeddingProviderType {
    MOCK,
    HTTP;

    public static EmbeddingProviderType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return MOCK;
        }
        return EmbeddingProviderType.valueOf(raw.trim().toUpperCase());
    }
}
