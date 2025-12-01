package com.neviswealth.searchservice.embedding;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpEmbeddingProvider implements EmbeddingProvider {

    private final RestClient restClient;

    public HttpEmbeddingProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public float[] embed(String text) {
        try {
            float[] response = restClient.post()
                    .uri("/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EmbedRequest(text))
                    .retrieve()
                    .body(float[].class);
            if (response == null) {
                throw new EmbeddingFailedException("EMBEDDING_EMPTY_RESPONSE", "Embedding service returned no vector");
            }
            return response;

        } catch (HttpMessageConversionException | ClassCastException e) {
            throw new EmbeddingFailedException("EMBEDDING_INVALID_FORMAT", "Embedding service returned invalid format", e);
        } catch (RestClientException e) {
            if (e.getCause() instanceof HttpMessageConversionException) {
                throw new EmbeddingFailedException("EMBEDDING_INVALID_FORMAT", "Embedding service returned invalid format", e);
            }
            throw new EmbeddingFailedException("EMBEDDING_CALL_FAILED", "Failed to call embedding service", e);
        }
    }

    private record EmbedRequest(String text) {
    }
}
