package com.dnnthanh.wallet.be.platform.cache;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import org.junit.jupiter.api.Test;

class CacheStarterAutoConfigurationImportsTest {
    private static final String IMPORTS_RESOURCE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void shouldPublishRedisCacheAutoConfigurationForStarterDiscovery() throws IOException {
        Enumeration<URL> resources =
                Thread.currentThread().getContextClassLoader().getResources(IMPORTS_RESOURCE);
        StringBuilder discoveredImports = new StringBuilder();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try (InputStream input = resource.openStream()) {
                discoveredImports.append(new String(input.readAllBytes(), UTF_8)).append('\n');
            }
        }

        assertThat(discoveredImports.toString())
                .as("cache starter must publish Spring Boot auto-configuration imports")
                .contains(WalletRedisCacheAutoConfiguration.class.getName());
    }
}
