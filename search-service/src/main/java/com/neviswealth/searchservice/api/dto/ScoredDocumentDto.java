package com.neviswealth.searchservice.api.dto;

import java.util.function.Function;

public record ScoredDocumentDto(
        DocumentDto document,
        double score,
        String matchedSnippet,
        boolean lexically
) {

    public ScoredDocumentDto updateScore(Function<Double, Double> update) {
        return new ScoredDocumentDto(
                document,
                update.apply(score),
                matchedSnippet,
                lexically
        );
    }
}
