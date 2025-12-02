package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.chunking.Chunk;
import com.neviswealth.searchservice.chunking.ChunkingFailedException;
import com.neviswealth.searchservice.chunking.ChunkingStrategy;
import com.neviswealth.searchservice.domain.Document;
import com.neviswealth.searchservice.domain.DocumentChunk;
import com.neviswealth.searchservice.embedding.EmbeddingFailedException;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.persistence.DocumentRepository;
import com.neviswealth.searchservice.summary.SummaryProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ChunkingStrategy chunkingStrategy;
    @Mock
    private EmbeddingProvider embeddingProvider;
    @Mock
    private SummaryProvider summaryProvider;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void createsDocumentAndPersistsChunks() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Utility bill")).thenReturn(false);
        when(chunkingStrategy.chunk(any(), eq("content body"))).thenReturn(List.of(new Chunk(0, "content body")));
        when(embeddingProvider.embed("content body")).thenReturn(new float[]{0.1f, 0.2f});
        Document saved = new Document(UUID.randomUUID(), clientId, "Utility bill", "content body", "hash", null, OffsetDateTime.now());
        when(documentRepository.insert(any())).thenReturn(saved);

        DocumentDto dto = documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body"));

        assertThat(dto.id()).isEqualTo(saved.id());
        ArgumentCaptor<List<DocumentChunk>> captor = ArgumentCaptor.forClass(List.class);
        verify(documentRepository).insertChunks(any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void doesNotInsertChunksWhenChunkingReturnsEmpty() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Title")).thenReturn(false);
        when(chunkingStrategy.chunk(any(), eq("content body"))).thenReturn(List.of());
        Document saved = new Document(UUID.randomUUID(), clientId, "Title", "content body", "hash", null, OffsetDateTime.now());
        when(documentRepository.insert(any())).thenReturn(saved);

        documentService.createDocument(clientId, new CreateDocumentRequest("Title", "content body"));

        verify(documentRepository, never()).insertChunks(any(), any());
    }

    @Test
    void computesContentHashBeforeInsert() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Title")).thenReturn(false);
        when(chunkingStrategy.chunk(any(), eq("abc"))).thenReturn(List.of());
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        when(documentRepository.insert(docCaptor.capture())).thenReturn(
                new Document(UUID.randomUUID(), clientId, "Title", "abc", "hash", null, OffsetDateTime.now())
        );

        documentService.createDocument(clientId, new CreateDocumentRequest("Title", "abc"));

        String expectedHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"; // sha256 of "abc"
        assertThat(docCaptor.getValue().contentHash()).isEqualTo(expectedHash);
    }

    @Test
    void rejectsDuplicateTitle() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Utility bill")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body")));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
        assertThat(ex.getReason()).contains("same title");
    }

    @Test
    void failsWhenClientMissing() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body")));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Client not found");
    }

    @Test
    void getDocumentReturnsNotFoundWhenMissing() {
        UUID docId = UUID.randomUUID();
        when(documentRepository.findById(docId)).thenReturn(java.util.Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> documentService.getDocument(docId));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Document not found");
    }

    @Test
    void getDocumentReturnsDto() {
        UUID docId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Document doc = new Document(docId, clientId, "Title", "Body", "hash", null, OffsetDateTime.now());
        when(documentRepository.findById(docId)).thenReturn(java.util.Optional.of(doc));

        var dto = documentService.getDocument(docId);

        assertThat(dto.id()).isEqualTo(docId);
        assertThat(dto.content()).isEqualTo("Body");
    }

    @Test
    void generatesSummaryWhenMissing() {
        UUID docId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Document doc = new Document(docId, clientId, "Title", "Body", "hash", null, OffsetDateTime.now());
        when(documentRepository.findById(docId)).thenReturn(java.util.Optional.of(doc));
        when(summaryProvider.summary("Body")).thenReturn("short summary");

        var dto = documentService.getDocument(docId);

        assertThat(dto.summary()).isEqualTo("short summary");
        verify(summaryProvider, times(1)).summary("Body");
        verify(documentRepository).updateDocumentWithSummary(docId, "short summary");
    }

    @Test
    void reusesExistingSummaryWithoutCallingProvider() {
        UUID docId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Document doc = new Document(docId, clientId, "Title", "Body", "hash", "existing summary", OffsetDateTime.now());
        when(documentRepository.findById(docId)).thenReturn(java.util.Optional.of(doc));

        var dto = documentService.getDocument(docId);

        assertThat(dto.summary()).isEqualTo("existing summary");
        verify(summaryProvider, never()).summary(any());
        verify(documentRepository, never()).updateDocumentWithSummary(any(), any());
    }

    @Test
    void throwsWhenChunkingFails() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Title")).thenReturn(false);
        when(chunkingStrategy.chunk(any(), eq("content body"))).thenThrow(new ChunkingFailedException("chunk error"));
        Document saved = new Document(UUID.randomUUID(), clientId, "Title", "content body", "hash", null, OffsetDateTime.now());
        when(documentRepository.insert(any())).thenReturn(saved);

        var ex = assertThrows(ChunkingFailedException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Title", "content body")));
        assertThat(ex.getMessage()).contains("chunk error");
    }

    @Test
    void throwsWhenEmbeddingFails() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Title")).thenReturn(false);
        when(chunkingStrategy.chunk(any(), eq("body"))).thenReturn(List.of(new Chunk(0, "body")));
        when(embeddingProvider.embed("body")).thenThrow(new EmbeddingFailedException("embed failed"));
        Document saved = new Document(UUID.randomUUID(), clientId, "Title", "body", "hash", null, OffsetDateTime.now());
        when(documentRepository.insert(any())).thenReturn(saved);

        var ex = assertThrows(EmbeddingFailedException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Title", "body")));
        assertThat(ex.getMessage()).contains("embed failed");
    }
}
