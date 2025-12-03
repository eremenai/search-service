package com.neviswealth.searchservice.summary;

import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSummaryProvider implements SummaryProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpSummaryProvider.class);
    private final SummaryClient summaryClient;
    private final String promptEngineering;

    public HttpSummaryProvider(SummaryClient summaryClient, String promptEngineering) {
        this.summaryClient = summaryClient;
        this.promptEngineering = promptEngineering;
    }

    @Override
    public String summary(String text) {
        try {
            var prompt = promptEngineering != null ? promptEngineering.formatted(text) : text;

            SummaryClient.SummaryResponse response = summaryClient.summarize(new SummaryClient.SummaryRequest(prompt, 32, 80));

            return response == null ? null : response.summary();

        } catch (RetryableException e) {
            // all reties failed
            log.error("Error during querying for summary", e);
            throw new SummaryFailedException("Could not retrieve summary", e);
        } catch (FeignException e) {
            log.error("Summary service call failed", e);
            throw new SummaryFailedException("Summary service call failed", e);
        }
    }
}
