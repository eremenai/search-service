package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.SearchResultDto;
import com.neviswealth.searchservice.domain.Client;
import com.neviswealth.searchservice.domain.Document;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.persistence.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private EmbeddingProvider embeddingProvider;

    @InjectMocks
    private SearchService searchService;

    @Test
    void throwsOnBlankQuery() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> searchService.search("  ", null));
        assertThat(ex1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex1.getReason()).contains("Query 'q' is required");

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> searchService.search(null, null));
        assertThat(ex2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void throwsWhenClientIdProvidedButMissing() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> searchService.search("query", clientId));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Client not found");
    }

    @Test
    void searchesThroughDocuments() {
        UUID clientId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(embeddingProvider.embed("payment")).thenReturn(new float[]{0.1f});
        when(clientRepository.searchByNameOrDomain("payment", "payment", 20)).thenReturn(List.of());
        Document document = new Document(documentId, clientId, "Payment doc", "body", "hash", null, OffsetDateTime.now());
        when(documentRepository.searchDocumentsWithBestChunk(eq(clientId), any(float[].class), eq(10)))
                .thenReturn(List.of(new DocumentRepository.DocumentSearchRow(document, 0.3d, "matched")));

        List<SearchResultDto> results = searchService.search("payment", clientId);

        assertThat(results).hasSize(1);
        SearchResultDto docResult = results.getFirst();
        assertThat(docResult.type()).isEqualTo("document");
        assertThat(docResult.matchedSnippet()).isEqualTo("matched");
        assertThat(docResult.document().title()).isEqualTo("Payment doc");
    }

    @Test
    void runsClientSearchForNameQueries() {
        when(clientRepository.searchByNameOrDomain("john", "john", 20))
                .thenReturn(List.of(
                        new ClientRepository.ClientSearchRow(
                                new Client(
                                        UUID.randomUUID(),
                                        "john@example.com",
                                        "example.com",
                                        "examplecom",
                                        "John",
                                        "Smith",
                                        "john smith",
                                        "PT",
                                        OffsetDateTime.now()
                                ),
                                0.9d
                        )
                ));
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10))).thenReturn(List.of());
        when(embeddingProvider.embed("john")).thenReturn(new float[]{0.5f});

        List<SearchResultDto> results = searchService.search("john", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().client().email()).isEqualTo("john@example.com");
    }

    @Test
    void runsDocumentSearchWhenClientIdProvided() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(embeddingProvider.embed("query")).thenReturn(new float[]{0.2f});
        when(documentRepository.searchDocumentsWithBestChunk(eq(clientId), any(float[].class), eq(10)))
                .thenReturn(List.of(new DocumentRepository.DocumentSearchRow(
                        new Document(UUID.randomUUID(), clientId, "Doc", "c", "h", null, OffsetDateTime.now()),
                        0.1d,
                        "snippet"
                )));

        List<SearchResultDto> results = searchService.search("query", clientId);

        assertThat(results).anySatisfy(r -> assertThat(r.type()).isEqualTo("document"));
    }

    @Test
    void searchesClientsByEmail() {
        String emailQuery = "USER@Example.com";
        when(clientRepository.searchByEmail("user@example.com", 20)).thenReturn(List.of(
                new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(UUID.randomUUID(), "user@example.com", "example.com", "examplecom", "User", "One", "user one", null, OffsetDateTime.now()),
                        0.9d
                )
        ));
        when(embeddingProvider.embed(emailQuery)).thenReturn(new float[]{0.1f});
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10))).thenReturn(List.of());

        List<SearchResultDto> results = searchService.search(emailQuery, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().client().email()).isEqualTo("user@example.com");
        verify(clientRepository).searchByEmail("user@example.com", 20);
        verify(clientRepository, never()).searchByNameOrDomain(anyString(), anyString(), anyInt());
    }

    @Test
    void returnsOnlyClientsWithLimit() {
        String query = "client@example.com";
        when(embeddingProvider.embed(query)).thenReturn(new float[]{0.0f});
        var clients = java.util.stream.IntStream.range(0, 25)
                .mapToObj(i -> new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(UUID.randomUUID(), "c" + i + "@example.com", "example.com", "examplecom", "F" + i, "L" + i, "f" + i + " l" + i, null, OffsetDateTime.now()),
                        1.0 - i * 0.01))
                .toList();
        when(clientRepository.searchByEmail(query, 20)).thenReturn(clients);
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10)))
                .thenReturn(List.of());

        List<SearchResultDto> results = searchService.search(query, null);

        assertThat(results).hasSize(15); // overall limit caps at 15
        assertThat(results).allSatisfy(r -> assertThat(r.type()).isEqualTo("client"));
        assertThat(results.getFirst().score()).isGreaterThanOrEqualTo(results.getLast().score());
        verify(clientRepository).searchByEmail(query, 20);
        verify(clientRepository, never()).searchByNameOrDomain(anyString(), anyString(), anyInt());
    }

    @Test
    void returnsOnlyDocumentsWithLimit() {
        when(clientRepository.searchByNameOrDomain("docs", "docs", 20)).thenReturn(List.of());
        when(embeddingProvider.embed("docs")).thenReturn(new float[]{0.5f});
        var docs = java.util.stream.IntStream.range(0, 12)
                .mapToObj(i -> new DocumentRepository.DocumentSearchRow(
                        new Document(UUID.randomUUID(), UUID.randomUUID(), "Doc" + i, "b", "h", null, OffsetDateTime.now()),
                        0.1 + i * 0.01,
                        "snippet" + i
                ))
                .toList();
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10)))
                .thenReturn(docs);

        List<SearchResultDto> results = searchService.search("docs", null);

        assertThat(results).hasSize(10);
        assertThat(results).allSatisfy(r -> assertThat(r.type()).isEqualTo("document"));
    }

    @Test
    void limitsClientsAndDocumentsAndOverall() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        String query = "q@example.com";
        when(embeddingProvider.embed(query)).thenReturn(new float[]{0.3f});
        // create 30 client hits, expect max 20
        var clients = java.util.stream.IntStream.range(0, 30)
                .mapToObj(i -> new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(
                                UUID.randomUUID(),
                                "c" + i + "@x.com",
                                "x.com",
                                "xcom",
                                "F" + i,
                                "L" + i,
                                "f" + i + " l" + i,
                                null,
                                OffsetDateTime.now()
                        ),
                        1.0 - (i * 0.01)
                ))
                .toList();
        when(clientRepository.searchByEmail(query, 20)).thenReturn(clients);

        // create 15 document hits, expect max 10
        var docs = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> new DocumentRepository.DocumentSearchRow(
                        new Document(UUID.randomUUID(), clientId, "D" + i, "b", "h", null, OffsetDateTime.now()),
                        0.5 + (0.01 * i),
                        "s" + i
                ))
                .toList();
        when(documentRepository.searchDocumentsWithBestChunk(eq(clientId), any(float[].class), eq(10)))
                .thenReturn(docs);

        List<SearchResultDto> results = searchService.search(query, clientId);

        assertThat(results).hasSizeLessThanOrEqualTo(15); // overall limit
        long clientCount = results.stream().filter(r -> r.type().equals("client")).count();
        long docCount = results.stream().filter(r -> r.type().equals("document")).count();
        assertThat(clientCount).isLessThanOrEqualTo(20);
        assertThat(docCount).isLessThanOrEqualTo(10);
        assertThat(clientCount + docCount).isEqualTo(results.size());
    }

    @Test
    void sortsByScoreAcrossClientsAndDocuments() {
        when(embeddingProvider.embed("mix")).thenReturn(new float[]{0.4f});
        when(clientRepository.searchByNameOrDomain("mix", "mix", 20)).thenReturn(List.of(
                new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(UUID.randomUUID(), "a@x.com", "x.com", "xcom", "A", "X", "a x", null, OffsetDateTime.now()),
                        0.2d),
                new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(UUID.randomUUID(), "b@x.com", "x.com", "xcom", "B", "X", "b x", null, OffsetDateTime.now()),
                        0.9d)
        ));
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10)))
                .thenReturn(List.of(
                        new DocumentRepository.DocumentSearchRow(new Document(UUID.randomUUID(), UUID.randomUUID(), "DocLow", "b", "h", null, OffsetDateTime.now()), 0.2d, "low"),
                        new DocumentRepository.DocumentSearchRow(new Document(UUID.randomUUID(), UUID.randomUUID(), "DocHigh", "b", "h", null, OffsetDateTime.now()), 0.05d, "high")
                ));

        List<SearchResultDto> results = searchService.search("mix", null);

        // distances -> scores: high doc ~0.95, client 0.9, low doc ~0.83, low client 0.2
        assertThat(results.stream().map(SearchResultDto::type)).containsExactly("document", "client", "document", "client");
        assertThat(results.get(0).document().title()).isEqualTo("DocHigh");
        assertThat(results.get(1).client().email()).isEqualTo("b@x.com");
        assertThat(results.get(2).document().title()).isEqualTo("DocLow");
        assertThat(results.get(3).client().email()).isEqualTo("a@x.com");
    }

    @Test
    void highestScoreTakesTopSlots() {
        when(clientRepository.searchByEmail("take@x.com", 20)).thenReturn(List.of(
                new ClientRepository.ClientSearchRow(
                        new com.neviswealth.searchservice.domain.Client(UUID.randomUUID(), "a@x.com", "x.com", "xcom", "A", "X", "a x", null, OffsetDateTime.now()),
                        0.99d)
        ));
        when(embeddingProvider.embed("take@x.com")).thenReturn(new float[]{0.6f});
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10)))
                .thenReturn(List.of(
                        new DocumentRepository.DocumentSearchRow(new Document(UUID.randomUUID(), UUID.randomUUID(), "DocLow", "b", "h", null, OffsetDateTime.now()), 0.4d, "low"),
                        new DocumentRepository.DocumentSearchRow(new Document(UUID.randomUUID(), UUID.randomUUID(), "DocHigh", "b", "h", null, OffsetDateTime.now()), 0.01d, "high")
                ));

        List<SearchResultDto> results = searchService.search("take@x.com", null);

        assertThat(results.getFirst().type()).isEqualTo("document");
        assertThat(results.getFirst().document().title()).isEqualTo("DocHigh");
        assertThat(results.get(1).type()).isEqualTo("client");
        assertThat(results.get(2).document().title()).isEqualTo("DocLow");
        assertThat(results.getFirst().score()).isEqualTo(results.stream().mapToDouble(SearchResultDto::score).max().orElse(0.0));
    }

    @Test
    void handlesMultiWordQueryNormalization() {
        String query = "John    Doe Wealth";
        when(embeddingProvider.embed(query)).thenReturn(new float[]{0.7f});
        when(clientRepository.searchByNameOrDomain("john doe wealth", "johndoewealth", 20)).thenReturn(List.of());
        when(documentRepository.searchDocumentsWithBestChunk(isNull(), any(float[].class), eq(10))).thenReturn(List.of());

        searchService.search(query, null);

        verify(clientRepository).searchByNameOrDomain("john doe wealth", "johndoewealth", 20);
        verify(clientRepository, never()).searchByEmail(anyString(), anyInt());
    }
}
