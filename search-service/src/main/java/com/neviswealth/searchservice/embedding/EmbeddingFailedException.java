package com.neviswealth.searchservice.embedding;

public class EmbeddingFailedException extends RuntimeException {

    private final String code;

    public EmbeddingFailedException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public EmbeddingFailedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
