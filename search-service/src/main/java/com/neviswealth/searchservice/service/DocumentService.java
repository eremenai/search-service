package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.api.dto.DocumentWithContentDto;
import com.neviswealth.searchservice.chunking.Chunk;
import com.neviswealth.searchservice.chunking.ChunkingStrategy;
import com.neviswealth.searchservice.domain.Document;
import com.neviswealth.searchservice.domain.DocumentChunk;
import com.neviswealth.searchservice.embedding.ChunkingFailedException;
import com.neviswealth.searchservice.embedding.DocumentIngestionException;
import com.neviswealth.searchservice.embedding.EmbeddingFailedException;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.persistence.DocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ClientRepository clientRepository;
    private final ChunkingStrategy chunkingStrategy;
    private final EmbeddingProvider embeddingProvider;

    public DocumentService(DocumentRepository documentRepository,
                           ClientRepository clientRepository,
                           ChunkingStrategy chunkingStrategy,
                           EmbeddingProvider embeddingProvider) {
        this.documentRepository = documentRepository;
        this.clientRepository = clientRepository;
        this.chunkingStrategy = chunkingStrategy;
        this.embeddingProvider = embeddingProvider;
    }

    @Transactional
    public DocumentDto createDocument(UUID clientId, CreateDocumentRequest request) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }
        String title = request.title().trim();
        String content = request.content().trim();
        if (documentRepository.existsByClientIdAndTitle(clientId, title)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document with the same title already exists for this client");
        }

        Document document = new Document(
                null,
                clientId,
                title,
                content,
                computeContentHash(content),
                null,
                null
        );
        Document saved = documentRepository.insert(document);

        try {
            List<Chunk> chunks = chunkContent(saved);
            List<DocumentChunk> toPersist = embedChunks(saved, chunks);
            if (!toPersist.isEmpty()) {
                documentRepository.insertChunks(saved.id(), toPersist);
            }
            return DocumentDto.from(saved);

        } catch (ChunkingFailedException e) {
            throw new DocumentIngestionException("CHUNKING_FAILED",
                    "Chunking failed for document '%s'".formatted(title),
                    clientId,
                    title,
                    e);
        } catch (EmbeddingFailedException e) {
            throw new DocumentIngestionException("EMBEDDING_FAILED",
                    "Embedding failed for document '%s'".formatted(title),
                    clientId,
                    title,
                    e);
        } catch (DocumentIngestionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DocumentIngestionException("DOCUMENT_INGESTION_FAILED",
                    "Failed to ingest document '%s' for client %s".formatted(title, clientId),
                    clientId,
                    title,
                    e);
        }
    }

    public DocumentWithContentDto getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .map(DocumentWithContentDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    private List<Chunk> chunkContent(Document document) {
        try {
            return chunkingStrategy.chunk(document.title(), document.content());
        } catch (ChunkingFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ChunkingFailedException("Chunking failed", e);
        }
    }

    private List<DocumentChunk> embedChunks(Document saved, List<Chunk> chunks) {
        try {
            List<DocumentChunk> toPersist = new ArrayList<>(chunks.size());
            for (Chunk chunk : chunks) {
                float[] embedding = embeddingProvider.embed(chunk.content());
                toPersist.add(new DocumentChunk(saved.id(), chunk.index(), chunk.content(), embedding));
            }
            return toPersist;
        } catch (EmbeddingFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EmbeddingFailedException("EMBEDDING_CALL_FAILED", "Embedding failed", e);
        }
    }

    private String computeContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    public List<DocumentDto> getAll() {
        return documentRepository.getAll().stream().map(DocumentDto::from).toList();
    }
}
