package com.neviswealth.searchservice.embedding;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class EmbeddingErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingErrorDecoder.class);

    @Override
    public Exception decode(String method, Response response) {
        var status = HttpStatus.valueOf(response.status());

        if (status.is4xxClientError()) {
            logger.error("Bad request while querying for embeddings code: {}, response: {}", status, response);
            return new EmbeddingFailedException("Bad request while querying for embeddings code: " + status);
        } else if (status.is5xxServerError()) {
            logger.error("Internal server error while querying for embeddings code: {}, response: {}", status, response);
            return new EmbeddingFailedException("Internal server error while querying for embeddings code: " + status);
        }

        logger.error("Unknown error while querying for embeddings code: {}, response: {}", status, response);
        return new EmbeddingFailedException("Unknown error while querying for embeddings code: " + status);
    }
}
