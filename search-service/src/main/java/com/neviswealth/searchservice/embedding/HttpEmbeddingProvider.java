package com.neviswealth.searchservice.embedding;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class HttpEmbeddingProvider implements EmbeddingProvider {

    private final RestTemplate restTemplate;

    public HttpEmbeddingProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public float[] embed(String text) {
        try {
            float[] response = restTemplate.postForObject("/embed", new EmbedRequest(text), float[].class);

            if (response == null) {
                throw new EmbeddingFailedException("EMBEDDING_EMPTY_RESPONSE", "Embedding service returned no vector");
            }
            return response;

        } catch (HttpStatusCodeException e) {
            HttpStatusCode status = e.getStatusCode();

            if (status.is4xxClientError()) {
                throw new EmbeddingFailedException("INVALID_EMBEDDING_REQUEST", "External 4xx: " + status, e);
            } else if (status.is5xxServerError()) {
                throw new EmbeddingFailedException("EMBEDDING_SERVER_ERROR", "External 5xx: " + status, e);
            } else {
                throw new EmbeddingFailedException("EMBEDDING_CALL_FAILED", "Unexpected status: " + status, e);
            }
        } catch (HttpMessageNotReadableException e) {
            // JSON format is wrong, cannot map to float[]
            throw new EmbeddingFailedException("EMBEDDING_INVALID_FORMAT", "Embedding service returned invalid format", e);
        } catch (RestClientException e) {
            // timeouts, connection errors
            throw new EmbeddingFailedException("EMBEDDING_CALL_FAILED", "External call failed", e);
        }
    }

    private record EmbedRequest(String text) {
    }
}
