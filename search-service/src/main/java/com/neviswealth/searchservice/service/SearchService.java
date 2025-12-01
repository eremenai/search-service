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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class SearchService {

    private static final int MAX_CLIENT_RESULTS = 20;
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
        List<DocumentRepository.DocumentSearchRow> rows =
                documentRepository.searchDocumentsWithBestChunk(clientId, queryVector, MAX_DOCUMENT_RESULTS);

        return rows.stream()
                .map(row -> SearchResultDto.documentResult(
                        scoreFromDistance(row.distance()),
                        DocumentDto.from(row.document()),
                        row.matchedSnippet()))
                .sorted(Comparator.comparingDouble(SearchResultDto::score).reversed())
                .toList();
    }

    private double scoreFromDistance(double distance) {
        return 1.0d / (1.0d + Math.max(distance, 0.0d));
    }
}
