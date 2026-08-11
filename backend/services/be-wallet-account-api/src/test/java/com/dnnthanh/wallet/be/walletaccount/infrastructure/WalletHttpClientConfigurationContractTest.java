package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class WalletHttpClientConfigurationContractTest {
    @Test
    void configuresExplicitConnectAndReadTimeoutsForHttpDependencies() throws IOException {
        Path applicationYaml = Path.of("src", "main", "resources", "application.yml");
        String configuration = Files.readString(applicationYaml);

        assertThat(configuration).contains("connect-timeout: ${WALLET_HTTP_CONNECT_TIMEOUT:2s}");
        assertThat(configuration).contains("read-timeout: ${WALLET_HTTP_READ_TIMEOUT:3s}");
    }
}
