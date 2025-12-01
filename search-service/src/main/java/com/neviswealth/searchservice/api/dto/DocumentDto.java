package com.neviswealth.searchservice.api.dto;

import com.neviswealth.searchservice.domain.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        UUID clientId,
        String title,
        OffsetDateTime createdAt
) {
    public static DocumentDto from(Document document) {
        return new DocumentDto(
                document.id(),
                document.clientId(),
                document.title(),
                document.createdAt()
        );
    }
}
