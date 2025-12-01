package com.neviswealth.searchservice.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchResultDto(
        List<ScoredClientDto> clients,
        List<ScoredDocumentDto> documents
) {}

