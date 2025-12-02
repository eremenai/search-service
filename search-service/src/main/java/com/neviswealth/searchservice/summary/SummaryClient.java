package com.neviswealth.searchservice.summary;

import com.neviswealth.searchservice.config.SummaryFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "summaryClient",
        url = "${summary.http.base-url}",
        configuration = SummaryFeignConfig.class
)
public interface SummaryClient {

    @PostMapping("/summarize")
    SummaryResponse summarize(@RequestBody SummaryRequest request);

    record SummaryRequest(String text, int min_tokens, int max_tokens) {
    }

    record SummaryResponse(String summary) {
    }
}
