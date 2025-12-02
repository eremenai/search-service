package com.neviswealth.searchservice.config;

public enum ProviderType {
    MOCK,
    HTTP;

    public static ProviderType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return MOCK;
        }
        return ProviderType.valueOf(raw.trim().toUpperCase());
    }
}
