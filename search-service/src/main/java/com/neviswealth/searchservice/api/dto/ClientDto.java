package com.neviswealth.searchservice.api.dto;

import com.neviswealth.searchservice.domain.Client;

import java.util.UUID;

public record ClientDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String countryOfResidence
) {
    public static ClientDto from(Client client) {
        return new ClientDto(
                client.id(),
                client.email(),
                client.firstName(),
                client.lastName(),
                client.countryOfResidence()
        );
    }
}
