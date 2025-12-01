package com.neviswealth.searchservice.domain;

import java.util.UUID;

public record DocumentChunk(
        UUID documentId,
        int chunkIndex,
        String content,
        float[] embedding
) {
}
