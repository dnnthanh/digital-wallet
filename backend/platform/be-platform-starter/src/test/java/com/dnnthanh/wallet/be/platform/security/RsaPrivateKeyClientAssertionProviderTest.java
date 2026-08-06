package com.dnnthanh.wallet.be.platform.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

class RsaPrivateKeyClientAssertionProviderTest {
    @TempDir Path tempDir;

    @Test
    void shouldSignShortLivedRfc7523AssertionAndExposeOnlyPublicJwk() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        Path privateKeyFile = tempDir.resolve("client-private.pem");
        String encodedPrivateKey =
                Base64.getMimeEncoder(64, "\n".getBytes(UTF_8))
                        .encodeToString(keyPair.getPrivate().getEncoded());
        Files.writeString(
                privateKeyFile,
                "-----BEGIN PRIVATE KEY-----\n"
                        + encodedPrivateKey
                        + "\n-----END PRIVATE KEY-----\n");

        InternalSecurityProperties properties = new InternalSecurityProperties();
        properties.setTokenUri("http://keycloak/token");
        properties.setClientId("be-auth-api");
        properties.setPrivateKeyLocation(privateKeyFile.toUri().toString());
        properties.setKeyId("be-auth-api-key-1");
        properties.setAssertionTtl(Duration.ofSeconds(30));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        RsaPrivateKeyClientAssertionProvider provider =
                new RsaPrivateKeyClientAssertionProvider(
                        properties,
                        new DefaultResourceLoader(),
                        Clock.fixed(now, ZoneOffset.UTC));

        SignedJWT assertion = SignedJWT.parse(provider.assertion());

        assertThat(assertion.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.RS256);
        assertThat(assertion.getHeader().getKeyID()).isEqualTo("be-auth-api-key-1");
        assertThat(assertion.getJWTClaimsSet().getIssuer()).isEqualTo("be-auth-api");
        assertThat(assertion.getJWTClaimsSet().getSubject()).isEqualTo("be-auth-api");
        assertThat(assertion.getJWTClaimsSet().getAudience()).containsExactly("http://keycloak/token");
        assertThat(assertion.getJWTClaimsSet().getIssueTime().toInstant()).isEqualTo(now);
        assertThat(assertion.getJWTClaimsSet().getExpirationTime().toInstant())
                .isEqualTo(now.plusSeconds(30));
        assertThat(assertion.getJWTClaimsSet().getJWTID()).isNotBlank();
        assertThat(assertion.verify(new RSASSAVerifier((RSAPublicKey) keyPair.getPublic()))).isTrue();

        Map<String, Object> jwkSet = provider.jwkSet();
        assertThat(jwkSet).containsKey("keys");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwkSet.get("keys");
        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst()).containsEntry("kid", "be-auth-api-key-1");
        assertThat(keys.getFirst()).doesNotContainKey("d");
    }
}
