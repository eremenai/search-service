package com.neviswealth.searchservice.config;

import com.neviswealth.searchservice.chunking.ChunkingStrategy;
import com.neviswealth.searchservice.chunking.ParagraphChunkingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChunkingConfig {

    @Bean
    public ChunkingStrategy chunkingStrategy(ChunkingProperties properties) {
        return new ParagraphChunkingStrategy(properties.getMaxChars());
    }
}
