package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.domain.Client;
import com.neviswealth.searchservice.persistence.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createsClientWhenEmailIsUnique() {
        when(clientRepository.existsByEmail("john@example.com")).thenReturn(false);
        Client saved = new Client(
                UUID.randomUUID(),
                "john@example.com",
                "example.com",
                "examplecom",
                "John",
                "Smith",
                "john smith",
                "PT",
                OffsetDateTime.now()
        );
        when(clientRepository.insert(any())).thenReturn(saved);

        ClientDto result = clientService.createClient(new CreateClientRequest("John", "Smith", "john@example.com", "PT"));

        assertThat(result.id()).isEqualTo(saved.id());
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.firstName()).isEqualTo("John");
    }

    @Test
    void normalizesEmailAndComputesDomainBeforeInsert() {
        when(clientRepository.existsByEmail("john@example.com")).thenReturn(false);
        Client saved = new Client(
                UUID.randomUUID(),
                "john@example.com",
                "example.com",
                "examplecom",
                "John",
                "Smith",
                "john smith",
                "US",
                OffsetDateTime.now()
        );
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        when(clientRepository.insert(any())).thenReturn(saved);

        clientService.createClient(new CreateClientRequest(" John ", "Smith", "   John@Example.com  ", "US"));

        verify(clientRepository).insert(captor.capture());
        Client inserted = captor.getValue();
        assertThat(inserted.email()).isEqualTo("john@example.com");
        assertThat(inserted.emailDomain()).isEqualTo("example.com");
        assertThat(inserted.emailDomainSlug()).isEqualTo("example");
        assertThat(inserted.firstName()).isEqualTo("John");
        assertThat(inserted.fullName()).isEqualTo("john smith");
    }

    @Test
    void throwsConflictWhenEmailExists() {
        when(clientRepository.existsByEmail("john@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                clientService.createClient(new CreateClientRequest("John", "Smith", "john@example.com", "PT")));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
        assertThat(ex.getReason()).contains("email already exists");
    }

    @Test
    void rejectsNullEmail() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                clientService.createClient(new CreateClientRequest("John", "Smith", null, "PT")));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsEmailWithoutDomain() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                clientService.createClient(new CreateClientRequest("John", "Smith", "invalid-email", "PT")));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Email must contain domain");
    }

    @Test
    void throwsNotFoundWhenClientMissing() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(java.util.Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> clientService.getClient(id));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Client not found");
    }
}
