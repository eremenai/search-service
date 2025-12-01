package com.neviswealth.searchservice.api.dto;

public record ScoredClientDto(
        ClientDto client,
        double score
) {
}
