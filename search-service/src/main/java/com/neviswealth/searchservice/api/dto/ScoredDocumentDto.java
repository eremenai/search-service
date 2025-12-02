package com.neviswealth.searchservice.api.dto;

public record ScoredDocumentDto(
        DocumentDto document,
        double score,
        String matchedSnippet,
        boolean lexically
) {
}
