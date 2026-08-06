package com.dnnthanh.wallet.be.platform.security;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.CLIENT_ASSERTION_SIGN_FAILED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.CLIENT_PRIVATE_KEY_LOAD_FAILED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.CLIENT_PRIVATE_KEY_NOT_RSA;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.CLIENT_PUBLIC_KEY_DERIVATION_FAILED;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.wallet.be.platform.stereotype.Adapter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

@Adapter
@ConditionalOnProperty(
        prefix = "wallet.internal-security",
        name = {"token-uri", "client-id", "private-key-location"})
public class RsaPrivateKeyClientAssertionProvider
        implements ClientAssertionProvider, ClientJwkProvider {
    private static final Duration DEFAULT_ASSERTION_TTL = Duration.ofSeconds(30);

    private final InternalSecurityProperties properties;
    private final Clock clock;
    private final RSAPrivateCrtKey privateKey;
    private final RSAKey publicJwk;

    public RsaPrivateKeyClientAssertionProvider(
            InternalSecurityProperties properties, ResourceLoader resourceLoader) {
        this(properties, resourceLoader, Clock.systemUTC());
    }

    RsaPrivateKeyClientAssertionProvider(
            InternalSecurityProperties properties, ResourceLoader resourceLoader, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.privateKey =
                loadPrivateKey(resourceLoader.getResource(properties.getPrivateKeyLocation()));
        this.publicJwk = createPublicJwk(privateKey, properties.getKeyId());
    }

    @Override
    public String assertion() {
        Instant issuedAt = clock.instant();
        Duration ttl =
                Objects.isNull(properties.getAssertionTtl())
                        ? DEFAULT_ASSERTION_TTL
                        : properties.getAssertionTtl();
        String audience =
                StringUtils.hasText(properties.getClientAssertionAudience())
                        ? properties.getClientAssertionAudience()
                        : properties.getTokenUri();
        JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                        .issuer(properties.getClientId())
                        .subject(properties.getClientId())
                        .audience(audience)
                        .issueTime(Date.from(issuedAt))
                        .expirationTime(Date.from(issuedAt.plus(ttl)))
                        .jwtID(UUID.randomUUID().toString())
                        .build();
        SignedJWT jwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.RS256)
                                .type(JOSEObjectType.JWT)
                                .keyID(properties.getKeyId())
                                .build(),
                        claims);
        try {
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (JOSEException failure) {
            throw new IllegalStateException(CLIENT_ASSERTION_SIGN_FAILED, failure);
        }
    }

    @Override
    public Map<String, Object> jwkSet() {
        return new JWKSet(publicJwk).toJSONObject();
    }

    private static RSAPrivateCrtKey loadPrivateKey(Resource resource) {
        try {
            String pem = new String(resource.getInputStream().readAllBytes(), UTF_8);
            String encoded =
                    pem.replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s", "");
            PrivateKey parsed =
                    KeyFactory.getInstance("RSA")
                            .generatePrivate(
                                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encoded)));
            if (parsed instanceof RSAPrivateCrtKey rsaPrivateKey) return rsaPrivateKey;
            throw new IllegalStateException(CLIENT_PRIVATE_KEY_NOT_RSA);
        } catch (IOException | GeneralSecurityException | IllegalArgumentException failure) {
            throw new IllegalStateException(CLIENT_PRIVATE_KEY_LOAD_FAILED, failure);
        }
    }

    private static RSAKey createPublicJwk(RSAPrivateCrtKey privateKey, String keyId) {
        try {
            RSAPublicKey publicKey =
                    (RSAPublicKey)
                            KeyFactory.getInstance("RSA")
                                    .generatePublic(
                                            new RSAPublicKeySpec(
                                                    privateKey.getModulus(),
                                                    privateKey.getPublicExponent()));
            return new RSAKey.Builder(publicKey).keyID(keyId).algorithm(JWSAlgorithm.RS256).build();
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException(CLIENT_PUBLIC_KEY_DERIVATION_FAILED, failure);
        }
    }
}
