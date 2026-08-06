package com.dnnthanh.wallet.be.platform.security;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.SERVICE_ACCESS_TOKEN_MISSING;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.wallet.be.platform.exception.ServiceTokenAcquisitionException;
import com.dnnthanh.wallet.be.platform.stereotype.Adapter;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Adapter
@ConditionalOnProperty(
        prefix = "wallet.internal-security",
        name = {"token-uri", "client-id", "private-key-location"})
@RequiredArgsConstructor
public class ServiceTokenProvider {
    private static final String CLIENT_ASSERTION_TYPE =
            "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final RestClient.Builder restClientBuilder;
    private final InternalSecurityProperties properties;
    private final ClientAssertionProvider assertionProvider;
    private volatile String token;
    private volatile Instant expiresAt = Instant.EPOCH;

    public synchronized String token() {
        if (Objects.nonNull(token) && Instant.now().isBefore(expiresAt.minusSeconds(20)))
            return token;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_assertion_type", CLIENT_ASSERTION_TYPE);
        form.add("client_assertion", assertionProvider.assertion());
        TokenResponse response =
                restClientBuilder
                        .clone()
                        .baseUrl(properties.getTokenUri())
                        .build()
                        .post()
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(TokenResponse.class);
        if (Objects.isNull(response) || Objects.isNull(response.access_token()))
            throw new ServiceTokenAcquisitionException(SERVICE_ACCESS_TOKEN_MISSING);
        token = response.access_token();
        expiresAt = Instant.now().plusSeconds(Math.max(response.expires_in(), 30));
        return token;
    }

    private record TokenResponse(String access_token, long expires_in) {}
}
