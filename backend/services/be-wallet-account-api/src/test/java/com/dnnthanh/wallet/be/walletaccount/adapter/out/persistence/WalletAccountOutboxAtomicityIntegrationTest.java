package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.walletaccount.WalletAccountApiApplication;
import com.dnnthanh.wallet.be.walletaccount.application.event.WalletAccountCreatedPayload;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletOutboxPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = WalletAccountApiApplication.class,
        properties = {
            "wallet.account.kyc-base-url=http://localhost:65535",
            "wallet.account.auth-api-base-url=http://localhost:65535",
            "wallet.account.supported-currencies=VND",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
        })
class WalletAccountOutboxAtomicityIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-11T08:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private WalletAccountRepositoryPort repository;
    @Autowired private WalletOutboxPort outboxPort;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PlatformTransactionManager transactionManager;

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
    void successfulTransactionPersistsWalletAndMinimalCreatedOutbox() {
        WalletAccount wallet = wallet();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(
                status -> {
                    repository.save(wallet);
                    outboxPort.appendCreated(event(wallet));
                });

        assertThat(count("wallet_account")).isEqualTo(1);
        assertThat(count("wallet_outbox_event")).isEqualTo(1);
        String eventType =
                jdbcTemplate.queryForObject(
                        "select event_type from wallet_outbox_event limit 1", String.class);
        String payload =
                jdbcTemplate.queryForObject(
                        "select payload::text from wallet_outbox_event limit 1", String.class);
        assertThat(eventType).isEqualTo("WALLET_ACCOUNT_CREATED");
        assertThat(payload).contains(wallet.walletId().toString(), "user-1", "VND", "ACTIVE");
        assertThat(payload)
                .doesNotContain("scopePath", "/bank/demo-branch", "balance", "permissions", "scopes");
    }

    @Test
    void rollbackRemovesBothWalletAndOutboxRows() {
        WalletAccount wallet = wallet();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(
                        () ->
                                transaction.executeWithoutResult(
                                        status -> {
                                            repository.save(wallet);
                                            outboxPort.appendCreated(event(wallet));
                                            throw new IllegalStateException("rollback test");
                                        }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(count("wallet_account")).isZero();
        assertThat(count("wallet_outbox_event")).isZero();
    }

    private Integer count(String tableName) {
        return jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    }

    private static WalletAccountCreatedPayload event(WalletAccount wallet) {
        return new WalletAccountCreatedPayload(
                wallet.walletId(),
                wallet.userId(),
                wallet.currency(),
                wallet.status().name(),
                wallet.createdAt());
    }

    private static WalletAccount wallet() {
        return WalletAccount.create(
                UUID.randomUUID(), "user-1", "/bank/demo-branch", "VND", NOW);
    }
}
