package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.AbstractIntegrationTest;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class DocumentControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmbeddingProvider embeddingProvider;

    @Autowired
    private ClientController clientController;
    @Autowired
    private DocumentController documentController;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));
        clientController.createClient(new CreateClientRequest("Marcus", "Lee", "marcus.lee@northbank.com", "UK"));
    }

    @AfterEach
    void clean() {
        jdbcTemplate.execute("truncate table clients cascade");
    }

    @Test
    void temp() throws Exception {
    }
}
