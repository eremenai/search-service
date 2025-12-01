package com.neviswealth.searchservice;

import com.neviswealth.searchservice.config.ChunkingProperties;
import com.neviswealth.searchservice.config.EmbeddingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({EmbeddingProperties.class, ChunkingProperties.class})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
