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
    public List<Chunk> chunk(String fullContent) {
        if (fullContent == null || fullContent.isBlank()) {
            return List.of();
        }

        String[] paragraphs = fullContent.split("\\n\\s*\\n");
        List<Chunk> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String rawParagraph : paragraphs) {
            String paragraph = rawParagraph.strip();
            if (paragraph.isEmpty()) {
                continue;
            }
            if (paragraph.length() > maxChars) {
                List<String> splitParagraphs = splitLongParagraph(paragraph);
                for (String piece : splitParagraphs) {
                    appendPiece(piece, result, current);
                }
            } else {
                appendPiece(paragraph, result, current);
            }
        }

        if (!current.isEmpty()) {
            result.add(new Chunk(result.size(), current.toString()));
        }
        return result;
    }

    private void appendPiece(String paragraph, List<Chunk> chunks, StringBuilder current) {
        int spacer = current.isEmpty() ? 0 : 2; // account for \n\n between paragraphs
        if (!current.isEmpty() && current.length() + spacer + paragraph.length() > maxChars) {
            chunks.add(new Chunk(chunks.size(), current.toString()));
            current.setLength(0);
            current.append(paragraph);
        } else if (paragraph.length() > maxChars) {
            chunks.add(new Chunk(chunks.size(), paragraph.substring(0, Math.min(paragraph.length(), maxChars))));
            current.setLength(0);
        } else {
            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(paragraph);
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
