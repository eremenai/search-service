package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.summary.SummaryErrorDecoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

public class SummaryFeignConfig {

    @Bean
    public RequestInterceptor summaryAuthInterceptor(SummaryProperties properties) {
        return template -> {
            String token = properties.getHttp().getApiToken();
            if (token != null && !token.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        };
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new SummaryErrorDecoder();
    }

}
