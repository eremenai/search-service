package com.neviswealth.searchservice.chunking;

public class ChunkingFailedException extends RuntimeException {

    public ChunkingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChunkingFailedException(String message) {
        super(message);
    }
}
