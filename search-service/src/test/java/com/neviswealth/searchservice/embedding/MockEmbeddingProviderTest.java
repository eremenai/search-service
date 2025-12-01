package com.neviswealth.searchservice.embedding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockEmbeddingProviderTest {

    @Test
    void returnsDeterministicVectors() {
        MockEmbeddingProvider provider = new MockEmbeddingProvider(8);

        float[] first = provider.embed("hello world");
        float[] second = provider.embed("hello world");
        float[] different = provider.embed("another input");

        assertThat(first).hasSize(8);
        assertThat(second).containsExactly(first);
        assertThat(different).isNotEqualTo(first);
    }
}
