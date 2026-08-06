package com.dnnthanh.wallet.be.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.platform.security.ClientJwkProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClientJwksControllerTest {
    @Test
    void shouldExposeRawPublicJwksDocument() throws Exception {
        ClientJwkProvider provider = mock(ClientJwkProvider.class);
        when(provider.jwkSet())
                .thenReturn(
                        Map.of(
                                "keys",
                                List.of(
                                        Map.of(
                                                "kty", "RSA",
                                                "kid", "be-auth-api-key-1",
                                                "n", "public-modulus",
                                                "e", "AQAB"))));
        ObjectMapper objectMapper = new ObjectMapper();
        ClientJwksController controller = new ClientJwksController(provider, objectMapper);

        String response = controller.jwks();
        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("keys").get(0).path("kid").asText())
                .isEqualTo("be-auth-api-key-1");
        assertThat(json.toString()).doesNotContain("\"d\"");
    }
}
