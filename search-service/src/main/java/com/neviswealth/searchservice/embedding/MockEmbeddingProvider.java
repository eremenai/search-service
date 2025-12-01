package com.neviswealth.searchservice.embedding;

import java.util.Random;

public class MockEmbeddingProvider implements EmbeddingProvider {

    private final int dimension;

    public MockEmbeddingProvider(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public float[] embed(String text) {
        int seed = text == null ? 0 : text.hashCode();
        Random random = new Random(seed);
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (random.nextFloat() - 0.5f) * 2.0f;
        }
        return vector;
    }
}
