package com.neviswealth.searchservice.chunking;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into paragraph-based chunks capped by a max character length.
 */
public class ParagraphChunkingStrategy implements ChunkingStrategy {

    private final int maxChars;

    public ParagraphChunkingStrategy(int maxChars) {
        this.maxChars = Math.max(1, maxChars);
    }

    @Override
    public List<Chunk> chunk(String title, String fullContent) {
        if (fullContent == null || fullContent.isBlank()) {
            return List.of();
        }

        String[] paragraphs = fullContent.split("\\n\\s*\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder paragraphMerge = new StringBuilder();

        if (!title.isBlank()) {
            chunks.add(title);
        }

        for (String rawParagraph : paragraphs) {
            String paragraph = rawParagraph.strip();
            if (paragraph.isEmpty()) {
                continue;
            }
            if (paragraph.length() > maxChars) {
                if (!paragraphMerge.isEmpty()) {
                    chunks.add(paragraphMerge.toString());
                    paragraphMerge.setLength(0);
                }

                chunks.addAll(splitLongParagraph(paragraph));
            } else {
                appendPiece(paragraph, chunks, paragraphMerge);
            }
        }

        if (!paragraphMerge.isEmpty()) {
            chunks.add(paragraphMerge.toString());
        }

        List<Chunk> result = new ArrayList<>();
        for (int i = 0; i <= chunks.size() - 1; i++) {
            result.add(new Chunk(i, chunks.get(i)));
        }
        return result;
    }

    private void appendPiece(String paragraph, List<String> chunks, StringBuilder paragraphMerge) {
        if (!paragraphMerge.isEmpty() && paragraphMerge.length() + 2 + paragraph.length() > maxChars) {
            chunks.add(paragraphMerge.toString());
            paragraphMerge.setLength(0);
            paragraphMerge.append(paragraph);
            paragraphMerge.setLength(0);
        } else {
            if (!paragraphMerge.isEmpty()) {
                paragraphMerge.append("\n\n");
            }
            paragraphMerge.append(paragraph);
        }
    }

    private List<String> splitLongParagraph(String paragraph) {
        String[] words = paragraph.split("\\s+");
        List<String> pieces = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (builder.isEmpty()) {
                builder.append(word);
                continue;
            }
            if (builder.length() + 1 + word.length() > maxChars) {
                pieces.add(builder.toString());
                builder.setLength(0);
                builder.append(word);
            } else {
                builder.append(' ').append(word);
            }
        }
        if (!builder.isEmpty()) {
            pieces.add(builder.toString());
        }
        return pieces;
    }
}
