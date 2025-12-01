package com.neviswealth.searchservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Client(
        UUID id,
        String email,
        String emailDomain,
        String emailDomainSlug,
        String firstName,
        String lastName,
        String fullName,
        String countryOfResidence,
        OffsetDateTime createdAt
) {
}
