package com.neviswealth.searchservice.api.dto;

public record ScoredDocumentDto(
        DocumentDto document,
        double distance,
        String matchedSnippet
) {
}
