package com.neviswealth.searchservice.integration;

import com.neviswealth.searchservice.api.ClientController;
import com.neviswealth.searchservice.api.DocumentController;
import com.neviswealth.searchservice.api.dto.*;
import com.neviswealth.searchservice.chunking.Chunk;
import com.neviswealth.searchservice.chunking.ChunkingStrategy;
import com.neviswealth.searchservice.embedding.EmbeddingFailedException;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DocumentSearchTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("embedding.dimension", () -> 3);
    }

    @MockitoBean
    private EmbeddingProvider embeddingProvider;
    @MockitoBean
    private ChunkingStrategy chunkingStrategy;

    @Autowired
    private ClientController clientController;
    @Autowired
    private DocumentController documentController;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void clean() {
        jdbcTemplate.execute("truncate table clients cascade");
    }

    @Test
    void matching() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "My content"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content"));

        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "any");
        assertThat(result1.documents()).hasSize(2);
        assertThat(result1.documents()).extracting(ScoredDocumentDto::document).extracting(DocumentDto::title)
                .containsExactlyInAnyOrder("My title", "My other title");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::score)
                .containsExactlyInAnyOrder(1.0, 1.0);
    }

    @Test
    void closeVectors() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "My content"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content"));


        when(embeddingProvider.embed(any())).thenReturn(new float[]{1.2f, 2, 3});
        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "any");
        assertThat(result1.documents()).hasSize(2);
        assertThat(result1.documents()).extracting(ScoredDocumentDto::document).extracting(DocumentDto::title)
                .containsExactly("My other title", "My title");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::score)
                .containsExactly(0.8000000674277657, 0.7999999556690504);
    }

    @Test
    void searchingOneClient() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "My content"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content"));


        when(embeddingProvider.embed(any())).thenReturn(new float[]{1.2f, 2, 3});
        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}&clientId={client}", SearchResultDto.class, "any", li.id());
        assertThat(result1.documents()).hasSize(1);
        assertThat(result1.documents()).extracting(ScoredDocumentDto::document).extracting(DocumentDto::title)
                .containsExactly("My other title");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::score)
                .containsExactly(0.8000000674277657);
    }

    @Test
    void lexically() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "My content"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content ---- newbie"));


        when(embeddingProvider.embed(any())).thenReturn(new float[]{-3, 2, 3});
        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "newbie");
        assertThat(result1.documents()).hasSize(1);
        assertThat(result1.documents()).extracting(ScoredDocumentDto::document).extracting(DocumentDto::title)
                .containsExactly("My other title");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::matchedSnippet)
                .containsExactly("My other content ---- newbie");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::score)
                .containsExactly(1.0);
    }

    @Test
    void lexicallyClose() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "Not even close"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content ---- newbie"));


        when(embeddingProvider.embed(any())).thenReturn(new float[]{-3, 2, 3});
        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "tent ---- nebwei");
        assertThat(result1.documents()).hasSize(1);
        assertThat(result1.documents()).extracting(ScoredDocumentDto::document).extracting(DocumentDto::title)
                .containsExactly("My other title");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::matchedSnippet)
                .containsExactly("My other content ---- newbie");
        assertThat(result1.documents()).extracting(ScoredDocumentDto::score)
                .containsExactly(0.58064516);
    }

    @Test
    void throwsUnexistingClient() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "Not even close"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content ---- newbie"));


        when(embeddingProvider.embed(any())).thenReturn(new float[]{-3, 2, 3});
        ResponseEntity<SearchResultDto> result1 = testRestTemplate.getForEntity("/search?q={query}&clientId={client}", SearchResultDto.class, "tent ---- nebwei", UUID.randomUUID());
        assertThat(result1.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void throwsEmbeddingFailure() {
        when(chunkingStrategy.chunk(any(), any())).thenAnswer(i -> List.of(new Chunk(0, i.getArgument(0)), new Chunk(1, i.getArgument(1))));
        when(embeddingProvider.embed(any()))
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3})
                .thenReturn(new float[]{1.4f, 2, 3});

        ClientDto maria = clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        ClientDto li = clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));

        documentController.createDocument(maria.id(), new CreateDocumentRequest("My title", "Not even close"));
        documentController.createDocument(li.id(), new CreateDocumentRequest("My other title", "My other content ---- newbie"));


        when(embeddingProvider.embed(any())).thenThrow(EmbeddingFailedException.class);
        ResponseEntity<SearchResultDto> result1 = testRestTemplate.getForEntity("/search?q={query}", SearchResultDto.class, "tent ---- nebwei");
        assertThat(result1.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
