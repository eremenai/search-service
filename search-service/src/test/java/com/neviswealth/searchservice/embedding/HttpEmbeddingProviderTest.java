package com.neviswealth.searchservice.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpEmbeddingProviderTest {

    @Test
    void returnsVectorOnSuccess() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(once(), requestTo("http://localhost/embed"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("[1.0, 2.5]", MediaType.APPLICATION_JSON));

        HttpEmbeddingProvider provider = new HttpEmbeddingProvider(client);
        float[] vector = provider.embed("hello");

        assertThat(vector).containsExactly(1.0f, 2.5f);
        server.verify();
    }

    @Test
    void throwsWhenBodyIsMissing() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(requestTo("http://localhost/embed"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        HttpEmbeddingProvider provider = new HttpEmbeddingProvider(client);

        assertThatThrownBy(() -> provider.embed("text"))
                .isInstanceOf(EmbeddingFailedException.class)
                .hasMessageContaining("no vector");
    }

    @Test
    void propagatesHttpErrors() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(requestTo("http://localhost/embed"))
                .andRespond(withServerError());

        HttpEmbeddingProvider provider = new HttpEmbeddingProvider(client);

        assertThatThrownBy(() -> provider.embed("text"))
                .isInstanceOf(EmbeddingFailedException.class)
                .hasMessageContaining("Failed to call embedding service");
    }

    @Test
    void throwsWhenBodyHasWrongShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(requestTo("http://localhost/embed"))
                .andRespond(withSuccess("{\"vector\":[1,2]}", MediaType.APPLICATION_JSON));

        HttpEmbeddingProvider provider = new HttpEmbeddingProvider(client);

        assertThatThrownBy(() -> provider.embed("text"))
                .isInstanceOf(EmbeddingFailedException.class)
                .hasMessageContaining("invalid format");
    }
}
