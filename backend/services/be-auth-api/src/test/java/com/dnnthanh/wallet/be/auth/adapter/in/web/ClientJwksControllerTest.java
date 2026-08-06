package com.dnnthanh.wallet.be.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.platform.security.ClientJwkProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClientJwksControllerTest {
    @Test
    void shouldExposeRawPublicJwksDocument() {
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
        ClientJwksController controller = new ClientJwksController(provider);

        Map<String, Object> response = controller.jwks();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) response.get("keys");
        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst()).containsEntry("kid", "be-auth-api-key-1");
        assertThat(keys.getFirst()).doesNotContainKey("d");
    }
}
