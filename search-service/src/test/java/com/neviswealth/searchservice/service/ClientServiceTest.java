package com.neviswealth.searchservice.service;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.domain.Client;
import com.neviswealth.searchservice.persistence.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    void throwsConflictWhenEmailExists() {
        when(clientRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                clientService.createClient(new CreateClientRequest("John", "Smith", "john@example.com", "PT")));
    }
}
