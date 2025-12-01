package com.neviswealth.searchservice.chunking;

import java.util.List;

public interface ChunkingStrategy {
    List<Chunk> chunk(String fullContent);
}
