package com.neviswealth.searchservice;

import com.neviswealth.searchservice.config.ChunkingProperties;
import com.neviswealth.searchservice.config.EmbeddingProperties;
import com.neviswealth.searchservice.config.SearchingProperties;
import com.neviswealth.searchservice.config.SummaryProperties;
import com.neviswealth.searchservice.embedding.EmbeddingClient;
import com.neviswealth.searchservice.summary.SummaryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties({EmbeddingProperties.class, ChunkingProperties.class, SearchingProperties.class, SummaryProperties.class})
@EnableFeignClients(basePackageClasses = {EmbeddingClient.class, SummaryClient.class})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
