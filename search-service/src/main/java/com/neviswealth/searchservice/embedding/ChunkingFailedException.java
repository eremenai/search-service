package com.neviswealth.searchservice.embedding;

public class ChunkingFailedException extends RuntimeException {

    private final String code;

    public ChunkingFailedException(String message, Throwable cause) {
        super(message, cause);
        this.code = "CHUNKING_FAILED";
    }

    public ChunkingFailedException(String message) {
        super(message);
        this.code = "CHUNKING_FAILED";
    }

    public String getCode() {
        return code;
    }
}
