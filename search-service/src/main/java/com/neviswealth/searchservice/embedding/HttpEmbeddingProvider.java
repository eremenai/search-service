package com.neviswealth.searchservice.embedding;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class HttpEmbeddingProvider implements EmbeddingProvider {

    private final RestClient restClient;

    public HttpEmbeddingProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public float[] embed(String text) {
        float[] response = restClient.post()
                .uri("/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new EmbedRequest(text))
                .retrieve()
                .body(float[].class);
        if (response == null) {
            throw new IllegalStateException("Embedding service returned no vector");
        }
        return response;
    }

    private record EmbedRequest(String text) {
    }
}
