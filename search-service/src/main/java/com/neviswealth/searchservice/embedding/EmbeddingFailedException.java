package com.neviswealth.searchservice.embedding;

public class EmbeddingFailedException extends RuntimeException {

    public EmbeddingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmbeddingFailedException(String message) {
        super(message);
    }
}
