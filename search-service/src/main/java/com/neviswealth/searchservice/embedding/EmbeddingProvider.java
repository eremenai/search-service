package com.neviswealth.searchservice.embedding;

public interface EmbeddingProvider {
    float[] embed(String text);
}
