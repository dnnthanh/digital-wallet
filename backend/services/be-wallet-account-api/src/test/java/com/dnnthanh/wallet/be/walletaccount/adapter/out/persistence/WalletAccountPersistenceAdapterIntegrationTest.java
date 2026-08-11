package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.WalletAccountApiApplication;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = WalletAccountApiApplication.class,
        properties = {
            "wallet.account.kyc-base-url=http://localhost:65535",
            "wallet.account.auth-api-base-url=http://localhost:65535",
            "wallet.account.supported-currencies=VND,USD",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
        })
class WalletAccountPersistenceAdapterIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-11T08:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private WalletAccountRepositoryPort repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from wallet_outbox_event");
        jdbcTemplate.update("delete from wallet_account");
    }

    @Test
    void persistsAndLoadsOwnedWalletWithoutBalanceColumns() {
        UUID walletId = UUID.randomUUID();
        repository.save(WalletAccount.create(walletId, "user-1", "/bank/demo-branch", "VND", NOW));

        assertThat(repository.findByIdAndUserId(walletId, "user-1")).isPresent();
        assertThat(repository.findByIdAndUserId(walletId, "other-user")).isEmpty();
        var columns =
                jdbcTemplate.queryForList(
                        "select column_name from information_schema.columns where table_name = 'wallet_account'",
                        String.class);
        assertThat(columns).noneMatch(column -> column.toLowerCase().contains("balance"));
    }

    @Test
    void databaseUniqueConstraintMapsDuplicateUserCurrencyToStableConflict() {
        repository.save(
                WalletAccount.create(UUID.randomUUID(), "user-1", "/bank/demo-branch", "VND", NOW));

        assertThatThrownBy(
                        () ->
                                repository.save(
                                        WalletAccount.create(
                                                UUID.randomUUID(),
                                                "user-1",
                                                "/bank/demo-branch",
                                                "VND",
                                                NOW.plusSeconds(1))))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(WalletAccountErrorCode.WALLET_ALREADY_EXISTS));
    }
}
