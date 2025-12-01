package com.neviswealth.searchservice.chunking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParagraphChunkingStrategyTest {

    @Test
    void splitsLongContentIntoChunksRespectingMaxChars() {
        String content = """
                Paragraph one text.

                Paragraph two is a bit longer than the first one and should be grouped with the previous text when possible.

                A very long paragraph that will exceed the configured limit because it contains many words that keep flowing without much pause to make sure the logic splits it appropriately into smaller pieces.
                """;
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(80);

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.getFirst().content().length()).isLessThanOrEqualTo(80);
        assertThat(chunks.get(1).content().length()).isLessThanOrEqualTo(80);
        assertThat(chunks.get(2).content().length()).isLessThanOrEqualTo(80);
        assertThat(chunks.get(0).index()).isZero();
        assertThat(chunks.get(1).index()).isEqualTo(1);
    }
}
