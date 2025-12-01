package com.neviswealth.searchservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Document(
        UUID id,
        UUID clientId,
        String title,
        String content,
        String contentHash,
        String summary,
        OffsetDateTime createdAt
) {
}
