package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.chunking.Chunk;
import com.neviswealth.searchservice.chunking.ChunkingStrategy;
import com.neviswealth.searchservice.domain.Document;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.persistence.DocumentRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private DocumentService documentService;

    @Test
    void createsDocumentAndPersistsChunks() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Utility bill")).thenReturn(false);
        when(chunkingStrategy.chunk("content body")).thenReturn(List.of(new Chunk(0, "content body")));
        when(embeddingProvider.embed("content body")).thenReturn(new float[]{0.1f, 0.2f});
        Document saved = new Document(UUID.randomUUID(), clientId, "Utility bill", "content body", "hash", null, OffsetDateTime.now());
        when(documentRepository.insert(any())).thenReturn(saved);

        DocumentDto dto = documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body"));

        assertThat(dto.id()).isEqualTo(saved.id());
        ArgumentCaptor<List<?>> captor = ArgumentCaptor.forClass(List.class);
        verify(documentRepository).insertChunks(any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void rejectsDuplicateTitle() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.existsByClientIdAndTitle(clientId, "Utility bill")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body")));
    }

    @Test
    void failsWhenClientMissing() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.existsById(clientId)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                documentService.createDocument(clientId, new CreateDocumentRequest("Utility bill", "content body")));
    }
}
