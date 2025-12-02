package com.neviswealth.searchservice.summary;

import java.util.concurrent.atomic.AtomicInteger;

public class MockSummaryProvider implements SummaryProvider {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String summary(String content) {
        return String.valueOf(counter.incrementAndGet());
    }
}
