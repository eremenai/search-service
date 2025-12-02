package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.*;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    private static final int MAX_CLIENT_RESULTS = 20;
    private static final int MAX_DOCUMENT_RESULTS = 10;

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

    public SearchResultDto search(String query, UUID clientId) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query 'q' is required");
        }
        if (clientId != null) {
            if (!clientRepository.existsById(clientId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
            }
        }

        return new SearchResultDto(searchClients(query), searchDocuments(query, clientId));
    }

    private List<ScoredClientDto> searchClients(String query) {
        String normalizedQuery = query.trim().toLowerCase().replaceAll("\\s+", " ");
        String slugQuery = SlugUtil.slugify(normalizedQuery);
        if (slugQuery.isEmpty()) {
            slugQuery = normalizedQuery;
        }
        boolean emailQuery = normalizedQuery.contains("@");

        List<ClientRepository.ClientSearchRow> clientHits = emailQuery
                ? clientRepository.searchByEmail(normalizedQuery, MAX_CLIENT_RESULTS)
                : clientRepository.searchByNameOrDomain(normalizedQuery, slugQuery, MAX_CLIENT_RESULTS);

        return clientHits.stream()
                .limit(MAX_CLIENT_RESULTS)
                .map(hit -> new ScoredClientDto(ClientDto.from(hit.client()), hit.score()))
                .toList();
    }

    private List<ScoredDocumentDto> searchDocuments(String query, UUID clientId) {
        List<DocumentRepository.DocumentSearchRow> lexically =
                documentRepository.searchLexically(clientId, query, MAX_DOCUMENT_RESULTS);

        List<DocumentRepository.DocumentSearchRow> byEmbeddings =
                documentRepository.searchWithEmbeddings(clientId, embeddingProvider.embed(query), MAX_DOCUMENT_RESULTS);


        // todo could be optimized
        return Stream.concat(lexically.stream(), byEmbeddings.stream())
                .map(row -> new ScoredDocumentDto(DocumentDto.from(row.document()), row.score(), row.matchedSnippet(), row.lexically()))
                .sorted(Comparator.comparingDouble(ScoredDocumentDto::score).reversed())
                // only unique documents
                .collect(Collectors.toMap(
                        dto -> dto.document().id(),
                        Function.identity(),
                        (existing, _) -> existing
                ))
                .values().stream()
                .sorted(Comparator.comparingDouble(ScoredDocumentDto::score).reversed())
                .limit(MAX_DOCUMENT_RESULTS)
                .toList();
    }
}
