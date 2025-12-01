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

        assertThat(chunks).hasSize(6);
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.content()).isNotBlank();
            assertThat(chunk.content().length()).isLessThanOrEqualTo(80);
        });
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).index()).isEqualTo(i);
        }
        assertThat(chunks.getFirst().content()).startsWith("Paragraph one text.");
    }

    @Test
    void returnsEmptyListWhenContentIsBlank() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(50);

        assertThat(strategy.chunk("   ")).isEmpty();
        assertThat(strategy.chunk(null)).isEmpty();
    }

    @Test
    void keepsSingleParagraphIntactWhenBelowLimit() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(100);
        String content = "Short paragraph only.";

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().content()).isEqualTo(content);
        assertThat(chunks.getFirst().index()).isZero();
    }

    @Test
    void splitsSingleLongParagraphByWords() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(30);
        String content = "This single paragraph is intentionally quite long to force splitting by words.";

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.content().length()).isLessThanOrEqualTo(30));
        assertThat(chunks.getFirst().content()).startsWith("This single paragraph");
    }

    @Test
    void groupsMultipleParagraphsWhenTheyFit() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(120);
        String content = """
                First short paragraph.

                Second short paragraph.

                Third short paragraph.
                """;

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().content()).contains("Second short paragraph.");
    }

    @Test
    void splitsOnlyParagraphThatExceedsLimit() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(60);
        String content = """
                Fit paragraph.

                This paragraph however is going to exceed the limit so it must be split across chunks accordingly.

                Tail paragraph.
                """;

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.getFirst().content()).contains("Fit paragraph.");
        assertThat(chunks.stream().map(Chunk::content).anyMatch(s -> s.contains("Tail paragraph."))).isTrue();
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.content().length()).isLessThanOrEqualTo(60));
    }

    @Test
    void mergesFirstTwoParagraphsThenSplitsThird() {
        ParagraphChunkingStrategy strategy = new ParagraphChunkingStrategy(70);
        String content = """
                Short one.

                Short two.

                This third paragraph is intentionally long and should be broken into two separate chunks by words to respect the limit.
                """;

        List<Chunk> chunks = strategy.chunk(content);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).content()).contains("Short one.").contains("Short two.");
        assertThat(chunks.get(1).content()).isNotEmpty();
        assertThat(chunks.get(2).content()).isNotEmpty();
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.content().length()).isLessThanOrEqualTo(70));
    }
}
