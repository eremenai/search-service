package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.api.dto.DocumentWithContentDto;
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
import com.neviswealth.searchservice.util.SingleFlightLoader;
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
    private final SummaryProvider summaryProvider;
    private final SingleFlightLoader<UUID, String> summaryLoader;

    public DocumentService(DocumentRepository documentRepository,
                           ClientRepository clientRepository,
                           ChunkingStrategy chunkingStrategy,
                           EmbeddingProvider embeddingProvider,
                           SummaryProvider summaryProvider) {
        this.documentRepository = documentRepository;
        this.clientRepository = clientRepository;
        this.chunkingStrategy = chunkingStrategy;
        this.embeddingProvider = embeddingProvider;
        this.summaryProvider = summaryProvider;
        this.summaryLoader = new SingleFlightLoader<>();
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

        List<Chunk> chunks = chunkContent(title, content);
        List<DocumentChunk> toPersist = embedChunks(saved.id(), chunks);
        if (!toPersist.isEmpty()) {
            documentRepository.insertChunks(saved.id(), toPersist);
        }
        return DocumentDto.from(saved);
    }

    @Transactional
    public DocumentWithContentDto getDocument(UUID documentId) {
        var document = documentRepository.findById(documentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        var dto = DocumentWithContentDto.from(document);

        if (document.summary() == null) {
            dto = dto.addSummary(summaryLoader.load(documentId, () -> loadSummary(document)));
        }

        return dto;
    }

    private String loadSummary(Document document) {
        String summary = summaryProvider.summary(document.content());

        if (summary != null) {
            documentRepository.updateDocumentWithSummary(document.id(), summary);
        }

        return summary;
    }

    private List<Chunk> chunkContent(String title, String content) {
        try {
            return chunkingStrategy.chunk(title, content);
        } catch (ChunkingFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ChunkingFailedException("Chunking failed", e);
        }
    }

    private List<DocumentChunk> embedChunks(UUID id, List<Chunk> chunks) {
        try {
            List<DocumentChunk> toPersist = new ArrayList<>(chunks.size());
            for (Chunk chunk : chunks) {
                float[] embedding = embeddingProvider.embed(chunk.content());
                toPersist.add(new DocumentChunk(id, chunk.index(), chunk.content(), embedding));
            }
            return toPersist;
        } catch (EmbeddingFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EmbeddingFailedException("Embedding failed", e);
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
