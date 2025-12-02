package com.neviswealth.searchservice.api.dto;

import com.neviswealth.searchservice.domain.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentWithContentDto(
        UUID id,
        UUID clientId,
        String title,
        String content,
        String summary,
        OffsetDateTime createdAt
) {
    public static DocumentWithContentDto from(Document document) {
        return new DocumentWithContentDto(
                document.id(),
                document.clientId(),
                document.title(),
                document.content(),
                document.summary(),
                document.createdAt()
        );
    }

    public DocumentWithContentDto addSummary(String summary) {
        return new DocumentWithContentDto(
                id,
                clientId,
                title,
                content,
                summary,
                createdAt
        );
    }
}
