package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.api.dto.SearchResultDto;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.persistence.DocumentRepository;
import com.neviswealth.searchservice.util.SlugUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class SearchService {

    private static final int MAX_CLIENT_RESULTS = 20;
    private static final int MAX_DOCUMENT_CHUNK_CANDIDATES = 50;
    private static final int MAX_DOCUMENT_RESULTS = 10;
    private static final int MAX_OVERALL_RESULTS = 15;

    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingProvider embeddingProvider;

    public SearchService(ClientRepository clientRepository,
                         DocumentRepository documentRepository,
                         EmbeddingProvider embeddingProvider) {
        this.clientRepository = clientRepository;
        this.documentRepository = documentRepository;
        this.embeddingProvider = embeddingProvider;
    }

    public List<SearchResultDto> search(String query, UUID clientId) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query 'q' is required");
        }
        if (clientId != null) {
            if (!clientRepository.existsById(clientId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
            }
        }

        List<SearchResultDto> clientsResults = searchClients(query);
        List<SearchResultDto> documentsResults = searchDocuments(query, clientId);

        return Stream.concat(clientsResults.stream(), documentsResults.stream())
                .sorted(Comparator.comparingDouble(SearchResultDto::score).reversed())
                .limit(MAX_OVERALL_RESULTS)
                .toList();
    }

    private List<SearchResultDto> searchClients(String query) {
        String normalizedQuery = query.trim().toLowerCase();
        String slugQuery = SlugUtil.slugify(normalizedQuery);
        if (slugQuery.isEmpty()) {
            slugQuery = normalizedQuery;
        }
        boolean emailQuery = normalizedQuery.contains("@");

        List<ClientRepository.ClientSearchRow> clientHits = emailQuery
                ? clientRepository.searchByEmail(normalizedQuery, MAX_CLIENT_RESULTS)
                : clientRepository.searchByNameOrDomain(normalizedQuery, slugQuery, MAX_CLIENT_RESULTS);

        return clientHits.stream()
                .map(hit -> SearchResultDto.clientResult(hit.score(), ClientDto.from(hit.client())))
                .toList();
    }

    private List<SearchResultDto> searchDocuments(String query, UUID clientId) {
        float[] queryVector = embeddingProvider.embed(query);
        List<DocumentRepository.DocumentChunkSearchRow> chunkRows =
                documentRepository.searchChunks(clientId, queryVector, MAX_DOCUMENT_CHUNK_CANDIDATES);

        Map<UUID, DocumentHit> bestHits = new LinkedHashMap<>();
        for (DocumentRepository.DocumentChunkSearchRow row : chunkRows) {
            double score = scoreFromDistance(row.distance());
            DocumentDto documentDto = new DocumentDto(row.documentId(), row.clientId(), row.title(), row.createdAt());
            DocumentHit incoming = new DocumentHit(documentDto, score, row.chunkContent());
            bestHits.merge(row.documentId(), incoming, (existing, inc) -> inc.score() > existing.score() ? inc : existing);
        }
        return bestHits.values().stream()
                .sorted(Comparator.comparingDouble(DocumentHit::score).reversed())
                .limit(MAX_DOCUMENT_RESULTS)
                .map(hit -> SearchResultDto.documentResult(hit.score(), hit.document(), hit.matchedSnippet()))
                .toList();
    }

    private double scoreFromDistance(double distance) {
        return 1.0d / (1.0d + Math.max(distance, 0.0d));
    }

    private record DocumentHit(DocumentDto document, double score, String matchedSnippet) {
    }
}
