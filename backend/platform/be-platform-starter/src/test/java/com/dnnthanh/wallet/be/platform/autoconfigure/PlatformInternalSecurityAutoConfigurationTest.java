package com.dnnthanh.wallet.be.platform.autoconfigure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.platform.security.RsaPrivateKeyClientAssertionProvider;
import com.dnnthanh.wallet.be.platform.security.ServiceTokenProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

class PlatformInternalSecurityAutoConfigurationTest {
    @TempDir Path tempDir;

    @Test
    void shouldCreatePrivateKeyJwtBeansFromAutoConfiguration() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keyPair = generator.generateKeyPair();
        var privateKeyFile = tempDir.resolve("client-private.pem");
        var encoded =
                Base64.getMimeEncoder(64, "\n".getBytes(UTF_8))
                        .encodeToString(keyPair.getPrivate().getEncoded());
        Files.writeString(
                privateKeyFile,
                "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----\n");

        new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(PlatformInternalSecurityAutoConfiguration.class))
                .withBean(RestClient.Builder.class, RestClient::builder)
                .withPropertyValues(
                        "wallet.internal-security.token-uri=http://keycloak/token",
                        "wallet.internal-security.client-id=be-auth-api",
                        "wallet.internal-security.private-key-location=" + privateKeyFile.toUri())
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(context)
                                    .hasSingleBean(RsaPrivateKeyClientAssertionProvider.class);
                            assertThat(context).hasSingleBean(ServiceTokenProvider.class);
                        });
    }
}
