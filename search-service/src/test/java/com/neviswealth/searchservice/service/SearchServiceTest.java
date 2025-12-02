package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.ScoredDocumentDto;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void searchesClientsByEmail() {
        String emailQuery = "USER@Example.com";
        Client client = new Client(
                UUID.randomUUID(),
                "user@example.com",
                "example.com",
                "examplecom",
                "User",
                "One",
                "user one",
                "PT",
                OffsetDateTime.now()
        );
        when(clientRepository.searchByEmail("user@example.com", 20))
                .thenReturn(List.of(new ClientRepository.ClientSearchRow(client, 0.9d)));
        when(embeddingProvider.embed(emailQuery)).thenReturn(new float[]{0.1f});

        SearchResultDto result = searchService.search(emailQuery, null);

        assertThat(result.clients()).hasSize(1);
        assertThat(result.clients().getFirst().client().email()).isEqualTo("user@example.com");
        assertThat(result.clients().getFirst().score()).isEqualTo(0.9d);
        assertThat(result.documents()).isEmpty();
        verify(clientRepository).searchByEmail("user@example.com", 20);
        verify(clientRepository, never()).searchByNameOrDomain(anyString(), anyString(), anyInt());
    }

    @Test
    void searchesClientsByNameOrDomainWithNormalization() {
        String query = "  John    Doe Wealth ";
        Client client = new Client(
                UUID.randomUUID(),
                "john@example.com",
                "example.com",
                "examplecom",
                "John",
                "Doe",
                "john doe",
                "PT",
                OffsetDateTime.now()
        );
        when(clientRepository.searchByNameOrDomain("john doe wealth", "johndoewealth", 20))
                .thenReturn(List.of(new ClientRepository.ClientSearchRow(client, 0.8d)));
        when(embeddingProvider.embed(query)).thenReturn(new float[]{0.5f});

        SearchResultDto result = searchService.search(query, null);

        assertThat(result.clients()).hasSize(1);
        assertThat(result.clients().getFirst().client().email()).isEqualTo("john@example.com");
        assertThat(result.documents()).isEmpty();
        verify(clientRepository).searchByNameOrDomain("john doe wealth", "johndoewealth", 20);
        verify(clientRepository, never()).searchByEmail(anyString(), anyInt());
    }

    @Test
    void searchesDocumentsForGivenClient() {
        UUID clientId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(clientRepository.searchByNameOrDomain("payment", "payment", 20)).thenReturn(List.of());
        when(embeddingProvider.embed("payment")).thenReturn(new float[]{0.1f});
        Document document = new Document(documentId, clientId, "Payment doc", "body", "hash", null, OffsetDateTime.now());
        when(documentRepository.searchWithEmbeddings(eq(clientId), any(float[].class), eq(10)))
                .thenReturn(List.of(new DocumentRepository.DocumentSearchRow(document, 0.3d, "matched", false)));

        SearchResultDto result = searchService.search("payment", clientId);

        assertThat(result.clients()).isEmpty();
        assertThat(result.documents()).hasSize(1);
        assertThat(result.documents().getFirst().document().id()).isEqualTo(documentId);
        assertThat(result.documents().getFirst().matchedSnippet()).isEqualTo("matched");
        assertThat(result.documents().getFirst().score()).isEqualTo(0.3d);
        verify(clientRepository).existsById(clientId);
        verify(documentRepository).searchWithEmbeddings(eq(clientId), any(float[].class), eq(10));
    }

    @Test
    void limitsClientResultsToTwenty() {
        String query = "client@example.com";
        when(embeddingProvider.embed(query)).thenReturn(new float[]{0.0f});
        List<ClientRepository.ClientSearchRow> clients = IntStream.range(0, 25)
                .mapToObj(i -> new ClientRepository.ClientSearchRow(
                        new Client(
                                UUID.randomUUID(),
                                "c" + i + "@example.com",
                                "example.com",
                                "examplecom",
                                "F" + i,
                                "L" + i,
                                "f" + i + " l" + i,
                                null,
                                OffsetDateTime.now()
                        ),
                        1.0 - i * 0.01
                ))
                .toList();
        when(clientRepository.searchByEmail(query, 20)).thenReturn(clients);
        when(documentRepository.searchWithEmbeddings(isNull(), any(float[].class), eq(10)))
                .thenReturn(List.of());

        SearchResultDto result = searchService.search(query, null);

        assertThat(result.clients()).hasSize(20);
        assertThat(result.documents()).isEmpty();
        verify(clientRepository).searchByEmail(query, 20);
    }

    @Test
    void sortsDocumentsByDistanceDescending() {
        when(clientRepository.searchByNameOrDomain("docs", "docs", 20)).thenReturn(List.of());
        when(embeddingProvider.embed("docs")).thenReturn(new float[]{0.1f});
        DocumentRepository.DocumentSearchRow docHigh = new DocumentRepository.DocumentSearchRow(
                new Document(UUID.randomUUID(), UUID.randomUUID(), "DocHigh", "b", "h", null, OffsetDateTime.now()),
                0.3d,
                "high",
                false
        );
        DocumentRepository.DocumentSearchRow docMid = new DocumentRepository.DocumentSearchRow(
                new Document(UUID.randomUUID(), UUID.randomUUID(), "DocMid", "b", "h", null, OffsetDateTime.now()),
                0.2d,
                "mid", false
        );
        DocumentRepository.DocumentSearchRow docLow = new DocumentRepository.DocumentSearchRow(
                new Document(UUID.randomUUID(), UUID.randomUUID(), "DocLow", "b", "h", null, OffsetDateTime.now()),
                0.1d,
                "low", false
        );
        when(documentRepository.searchWithEmbeddings(isNull(), any(float[].class), eq(10)))
                .thenReturn(List.of(docHigh, docLow, docMid));

        SearchResultDto result = searchService.search("docs", null);

        assertThat(result.documents()).extracting(ScoredDocumentDto::score)
                .containsExactly(0.3d, 0.2d, 0.1d);
        assertThat(result.documents().getFirst().matchedSnippet()).isEqualTo("high");
    }

    @Test
    void keepsHighestScorePerDocumentAcrossSources() {
        when(clientRepository.searchByNameOrDomain("dup", "dup", 20)).thenReturn(List.of());
        when(embeddingProvider.embed("dup")).thenReturn(new float[]{0.1f});
        UUID documentId = UUID.randomUUID();
        Document doc = new Document(documentId, UUID.randomUUID(), "DupDoc", "b", "h", null, OffsetDateTime.now());
        DocumentRepository.DocumentSearchRow lexicalHit = new DocumentRepository.DocumentSearchRow(doc, 0.4d, "lexical", false);
        DocumentRepository.DocumentSearchRow embeddingHit = new DocumentRepository.DocumentSearchRow(doc, 0.9d, "embed", false);

        when(documentRepository.searchLexically(isNull(), eq("dup"), eq(10))).thenReturn(List.of(lexicalHit));
        when(documentRepository.searchWithEmbeddings(isNull(), any(float[].class), eq(10))).thenReturn(List.of(embeddingHit));

        SearchResultDto result = searchService.search("dup", null);

        assertThat(result.documents()).hasSize(1);
        ScoredDocumentDto topDocument = result.documents().getFirst();
        assertThat(topDocument.document().id()).isEqualTo(documentId);
        assertThat(topDocument.score()).isEqualTo(0.9d);
        assertThat(topDocument.matchedSnippet()).isEqualTo("embed");
    }

    @Test
    void limitsDocumentResultsToTen() {
        when(clientRepository.searchByNameOrDomain("docs", "docs", 20)).thenReturn(List.of());
        when(embeddingProvider.embed("docs")).thenReturn(new float[]{0.1f});
        List<DocumentRepository.DocumentSearchRow> docs = IntStream.range(0, 12)
                .mapToObj(i -> new DocumentRepository.DocumentSearchRow(
                        new Document(UUID.randomUUID(), UUID.randomUUID(), "Doc" + i, "b", "h", null, OffsetDateTime.now()),
                        0.1 + i * 0.01,
                        "snippet" + i, false
                ))
                .toList();
        when(documentRepository.searchWithEmbeddings(isNull(), any(float[].class), eq(10)))
                .thenReturn(docs);

        SearchResultDto result = searchService.search("docs", null);

        assertThat(result.documents()).hasSize(10);
        assertThat(result.clients()).isEmpty();
    }
}
