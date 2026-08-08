package com.dnnthanh.wallet.be.kyc.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dnnthanh.wallet.be.kyc.CustomerKycApiApplication;
import com.dnnthanh.wallet.be.kyc.adapter.out.auth.AuthApiCurrentAuthorizationAdapter;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = {
            CustomerKycApiApplication.class,
            KycSecurityIntegrationTest.PublicProbeController.class
        },
        properties = {
            "wallet.kyc.security.document-hmac-secret=test-secret",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
        })
@AutoConfigureMockMvc
class KycSecurityIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthApiCurrentAuthorizationAdapter authorizationAdapter;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void publicRouteAllowsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/public/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void unauthenticatedPrivateKycRequestReturns401() throws Exception {
        mockMvc.perform(get("/private/api/v1/kyc/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void authenticatedUserWithoutEffectivePermissionReturns403() throws Exception {
        when(authorizationAdapter.currentAuthorization())
                .thenReturn(new CurrentAuthorization(Set.of(), Set.of("/bank/demo-branch")));

        mockMvc.perform(
                        get("/private/api/v1/kyc/me")
                                .with(jwt().jwt(token -> token.subject("user-123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @RestController
    static class PublicProbeController {
        @GetMapping("/public/ping")
        String ping() {
            return "pong";
        }
    }
}
