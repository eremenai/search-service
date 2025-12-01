package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.domain.Client;
import com.neviswealth.searchservice.persistence.ClientRepository;
import com.neviswealth.searchservice.util.SlugUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ClientDto createClient(CreateClientRequest request) {
        String email = normalizeEmail(request.email());
        if (clientRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client with this email already exists");
        }
        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();
        String fullName = (firstName + " " + lastName).trim().toLowerCase();
        String emailDomain = extractDomain(email);
        String emailDomainSlug = SlugUtil.slugify(emailDomain);

        Client toInsert = new Client(
                null,
                email,
                emailDomain,
                emailDomainSlug,
                firstName,
                lastName,
                fullName,
                request.countryOfResidence(),
                null
        );
        Client saved = clientRepository.insert(toInsert);
        return ClientDto.from(saved);
    }

    public ClientDto getClient(UUID id) {
        return clientRepository.findById(id)
                .map(ClientDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must contain domain");
        }
        return email.substring(atIndex + 1);
    }
}
