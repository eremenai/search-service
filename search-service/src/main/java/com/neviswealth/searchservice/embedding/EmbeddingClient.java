package com.neviswealth.searchservice.embedding;

import com.neviswealth.searchservice.config.EmbeddingFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "embeddingClient",
        url = "${embedding.http.base-url}",
        configuration = EmbeddingFeignConfig.class
)
public interface EmbeddingClient {

    @PostMapping("/embed")
    EmbeddingResponse embed(@RequestBody EmbeddingRequest request);

    record EmbeddingRequest(String text) {
    }

    record EmbeddingResponse(float[] embedding) {
    }
}
