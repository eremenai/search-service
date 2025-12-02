package com.neviswealth.searchservice.summary;

public class SummaryFailedException extends RuntimeException {

    public SummaryFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SummaryFailedException(String message) {
        super(message);
    }
}
