package com.neviswealth.searchservice.summary;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class SummaryErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(SummaryErrorDecoder.class);

    @Override
    public Exception decode(String method, Response response) {
        var status = HttpStatus.valueOf(response.status());

        if (status.is4xxClientError()) {
            logger.error("Bad request while querying for summary code: {}, response: {}", status, response);
            return new SummaryFailedException("Bad request while querying for summary code: " + status);
        } else if (status.is5xxServerError()) {
            logger.error("Internal server error while querying for summary code: {}, response: {}", status, response);
            return new SummaryFailedException("Internal server error while querying for summary code: " + status);
        }

        logger.error("Unknown error while querying for summary code: {}, response: {}", status, response);
        return new SummaryFailedException("Unknown error while querying for summary code: " + status);
    }
}
