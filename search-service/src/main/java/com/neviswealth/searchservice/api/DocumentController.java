package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.api.dto.DocumentDto;
import com.neviswealth.searchservice.api.dto.DocumentWithContentDto;
import com.neviswealth.searchservice.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/clients/{clientId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDto createDocument(@PathVariable("clientId") UUID clientId,
                                      @Valid @RequestBody CreateDocumentRequest request) {
        return documentService.createDocument(clientId, request);
    }

    @GetMapping("/documents/{id}")
    public DocumentWithContentDto getDocument(@PathVariable("id") UUID documentId) {
        return documentService.getDocument(documentId);
    }
}
