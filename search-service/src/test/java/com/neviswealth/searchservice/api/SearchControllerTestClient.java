package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.AbstractIntegrationTest;
import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.api.dto.ScoredClientDto;
import com.neviswealth.searchservice.api.dto.SearchResultDto;
import com.neviswealth.searchservice.embedding.EmbeddingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class SearchControllerTestClient extends AbstractIntegrationTest {

    @MockitoBean
    private EmbeddingProvider embeddingProvider;

    @Autowired
    private ClientController clientController;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        when(embeddingProvider.embed(anyString())).thenReturn(new float[]{0.0f});
        clientController.createClient(new CreateClientRequest("Maria", "Lopez", "maria.lopez@laurelwealth.com", "ES"));
        clientController.createClient(new CreateClientRequest("Li", "Chen", "li.chen@laurelwealth.com", "DE"));
        clientController.createClient(new CreateClientRequest("Marcus", "Wee", "marcus.Wee@northbank.com", "UK"));
    }

    @AfterEach
    void clean() {
        jdbcTemplate.execute("truncate table clients cascade");
    }

    @Test
    void searchByName() throws Exception {
        SearchResultDto result = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "Mar");
        assertThat(result.clients()).hasSize(2);
        assertThat(result.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactly("maria.lopez@laurelwealth.com", "marcus.wee@northbank.com");

        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "Mari");
        assertThat(result1.clients()).hasSize(2);
        assertThat(result1.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactly("maria.lopez@laurelwealth.com", "marcus.wee@northbank.com");

        SearchResultDto result2 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "Maria L");
        assertThat(result2.clients()).hasSize(1);
        assertThat(result2.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactly("maria.lopez@laurelwealth.com");
    }

    @Test
    void searchByLastName() throws Exception {
        SearchResultDto result = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "Chen");
        assertThat(result.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email).containsExactly("li.chen@laurelwealth.com");
    }

    @Test
    void searchByEmail() throws Exception {
        SearchResultDto result = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "@northnank.com");
        assertThat(result.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email).containsExactly("marcus.wee@northbank.com");

        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "wee@northnank");
        assertThat(result1.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email).containsExactly("marcus.wee@northbank.com");

        SearchResultDto result2 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "lopez@northbank.com");
        assertThat(result2.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email).containsExactly("marcus.wee@northbank.com");
    }

    @Test
    void searchByDomainOrFullNameFragmentsWithTypos() throws Exception {
        SearchResultDto result1 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "laurelwealth");
        assertThat(result1.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactlyInAnyOrder("maria.lopez@laurelwealth.com", "li.chen@laurelwealth.com");

        SearchResultDto result2 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "marcus lee");
        assertThat(result2.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactly("marcus.wee@northbank.com");

        SearchResultDto result3 = testRestTemplate.getForObject("/search?q={query}", SearchResultDto.class, "laurel weal");
        assertThat(result3.clients()).extracting(ScoredClientDto::client).extracting(ClientDto::email)
                .containsExactlyInAnyOrder("maria.lopez@laurelwealth.com", "li.chen@laurelwealth.com");
    }
}
