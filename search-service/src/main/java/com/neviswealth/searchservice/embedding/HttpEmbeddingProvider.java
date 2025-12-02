package com.neviswealth.searchservice.embedding;

import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpEmbeddingProvider.class);
    private final EmbeddingClient embeddingClient;

    public HttpEmbeddingProvider(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    @Override
    public float[] embed(String text) {
        try {
            EmbeddingClient.EmbeddingResponse response = embeddingClient.embed(new EmbeddingClient.EmbeddingRequest(text));
            float[] embedding = response == null ? null : response.embedding();

            if (embedding == null) {
                throw new EmbeddingFailedException("Embedding service returned no vector");
            }
            return embedding;

        } catch (RetryableException e) {
            // all reties failed
            log.error("Error during querying for embedding", e);
            throw new EmbeddingFailedException("Could not retrieve embeddings", e);
        } catch (DecodeException e) {
            log.error("Embedding service returned invalid format: {}", e.responseBody().orElse(null), e);
            throw new EmbeddingFailedException("Embedding service returned invalid format", e);
        } catch (FeignException e) {
            log.error("Embedding service call failed", e);
            throw new EmbeddingFailedException("Embedding service call failed", e);
        }
    }
}
