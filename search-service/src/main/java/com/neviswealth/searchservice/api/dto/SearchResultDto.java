package com.neviswealth.searchservice.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchResultDto(
        String type,
        double score,
        ClientDto client,
        DocumentDto document,
        String matchedSnippet
) {
    public static SearchResultDto clientResult(double score, ClientDto client) {
        return new SearchResultDto("client", score, client, null, null);
    }

    public static SearchResultDto documentResult(double score, DocumentDto document, String matchedSnippet) {
        return new SearchResultDto("document", score, null, document, matchedSnippet);
    }
}
