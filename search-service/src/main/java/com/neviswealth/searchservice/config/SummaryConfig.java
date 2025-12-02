package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.summary.HttpSummaryProvider;
import com.neviswealth.searchservice.summary.MockSummaryProvider;
import com.neviswealth.searchservice.summary.SummaryClient;
import com.neviswealth.searchservice.summary.SummaryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummaryConfig {
    @Bean
    public SummaryProvider summaryProvider(SummaryProperties properties, SummaryClient summaryClient) {
        return switch (properties.getProvider()) {
            case HTTP -> new HttpSummaryProvider(summaryClient);
            case MOCK -> new MockSummaryProvider();
        };
    }
}
