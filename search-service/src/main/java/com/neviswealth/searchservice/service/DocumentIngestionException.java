package com.neviswealth.searchservice.service;

import java.util.UUID;

public class DocumentIngestionException extends RuntimeException {

    private final String code;
    private final UUID clientId;
    private final String title;

    public DocumentIngestionException(String code, String message, UUID clientId, String title, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.clientId = clientId;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getTitle() {
        return title;
    }
}
