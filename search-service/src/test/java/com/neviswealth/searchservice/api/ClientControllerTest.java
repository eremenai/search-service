package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.AbstractIntegrationTest;
import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientControllerTest extends AbstractIntegrationTest {

    @Test
    void createsClient() throws Exception {
        CreateClientRequest request = new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "PT");

        ClientDto clientDto = testRestTemplate.postForObject("/clients", request, ClientDto.class);

        assertThat(clientDto.id()).isNotNull();
        assertThat(clientDto.firstName()).isEqualTo("Maria");
        assertThat(clientDto.lastName()).isEqualTo("Lopez");
        assertThat(clientDto.email()).isEqualTo("maria.lopez@laurelwealth.com");

        ClientDto found = testRestTemplate.getForObject("/clients/" + clientDto.id(), ClientDto.class);
        assertThat(found.id()).isEqualTo(clientDto.id());
        assertThat(found.firstName()).isEqualTo("Maria");
        assertThat(found.lastName()).isEqualTo("Lopez");
        assertThat(found.email()).isEqualTo("maria.lopez@laurelwealth.com");
    }

    @Test
    void badRequestIfNameIsBlank() throws Exception {
        CreateClientRequest request = new CreateClientRequest("", "Lopez", "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response = testRestTemplate.postForEntity("/clients", request, ClientDto.class);
        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        CreateClientRequest request1 = new CreateClientRequest(null, "Lopez", "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response1 = testRestTemplate.postForEntity("/clients", request1, ClientDto.class);
        assertThat(response1.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        CreateClientRequest request2 = new CreateClientRequest("   ", "Lopez", "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response2 = testRestTemplate.postForEntity("/clients", request2, ClientDto.class);
        assertThat(response2.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void badRequestIfLastNameIsBlank() throws Exception {
        CreateClientRequest request = new CreateClientRequest("Maria", "", "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response = testRestTemplate.postForEntity("/clients", request, ClientDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CreateClientRequest request1 = new CreateClientRequest("Maria", null, "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response1 = testRestTemplate.postForEntity("/clients", request1, ClientDto.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CreateClientRequest request2 = new CreateClientRequest("Maria", "   ", "maria.lopez@laurelwealth.com", "PT");
        ResponseEntity<ClientDto> response2 = testRestTemplate.postForEntity("/clients", request2, ClientDto.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void badRequestIfEmailIsInvalid() throws Exception {
        CreateClientRequest missingDomain = new CreateClientRequest("Sofia", "Romero", "sofia.romerogmail.com", "ES");
        ResponseEntity<ClientDto> response = testRestTemplate.postForEntity("/clients", missingDomain, ClientDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CreateClientRequest blankEmail = new CreateClientRequest("Sofia", "Romero", "   ", "ES");
        ResponseEntity<ClientDto> response1 = testRestTemplate.postForEntity("/clients", blankEmail, ClientDto.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CreateClientRequest nullEmail = new CreateClientRequest("Sofia", "Romero", null, "ES");
        ResponseEntity<ClientDto> response2 = testRestTemplate.postForEntity("/clients", nullEmail, ClientDto.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void conflictIfEmailAlreadyExists() throws Exception {
        CreateClientRequest request = new CreateClientRequest("Sam", "Peters", "sam.peters@dupclients.com", "US");

        ClientDto created = testRestTemplate.postForObject("/clients", request, ClientDto.class);
        assertThat(created.id()).isNotNull();

        ResponseEntity<ClientDto> secondAttempt = testRestTemplate.postForEntity("/clients", request, ClientDto.class);
        assertThat(secondAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getNotExisting() throws Exception {
        ResponseEntity<ClientDto> resp = testRestTemplate.getForEntity("/clients/" + UUID.randomUUID(), ClientDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
