package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.kyc.CustomerKycApiApplication;
import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import java.time.Instant;
import java.time.LocalDate;
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
        classes = CustomerKycApiApplication.class,
        properties = {
            "wallet.kyc.security.document-hmac-secret=test-secret",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
        })
class KycOutboxAtomicityIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private CustomerKycRepositoryPort repository;
    @Autowired private KycOutboxPort outboxPort;
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
        jdbcTemplate.update("delete from kyc_outbox_event");
        jdbcTemplate.update("delete from customer_kyc");
    }

    @Test
    void successfulTransactionPersistsKycAndMinimalOutboxPayload() {
        CustomerKyc draft = draft();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(
                status -> {
                    repository.save(draft);
                    outboxPort.appendStatusChanged(event(draft.kycId(), draft.userId()));
                });

        assertThat(count("customer_kyc")).isEqualTo(1);
        assertThat(count("kyc_outbox_event")).isEqualTo(1);
        String payload =
                jdbcTemplate.queryForObject(
                        "select payload::text from kyc_outbox_event limit 1", String.class);
        assertThat(payload).contains("PENDING_REVIEW");
        assertThat(payload).doesNotContain("Nguyen Van A", "fingerprint-1", "1 Nguyen Hue");
    }

    @Test
    void rollbackRemovesBothKycAndOutboxRows() {
        CustomerKyc draft = draft();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(
                        () ->
                                transaction.executeWithoutResult(
                                        status -> {
                                            repository.save(draft);
                                            outboxPort.appendStatusChanged(
                                                    event(draft.kycId(), draft.userId()));
                                            throw new IllegalStateException("rollback test");
                                        }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(count("customer_kyc")).isZero();
        assertThat(count("kyc_outbox_event")).isZero();
    }

    private Integer count(String tableName) {
        return jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    }

    private static KycStatusChangedPayload event(UUID kycId, String userId) {
        return new KycStatusChangedPayload(
                kycId, userId, KycStatus.DRAFT, KycStatus.PENDING_REVIEW, NOW);
    }

    private static CustomerKyc draft() {
        return CustomerKyc.createDraft(
                UUID.randomUUID(),
                "user-1",
                "/bank/demo-branch",
                new KycProfile(
                        "Nguyen Van A",
                        LocalDate.of(1998, 4, 12),
                        "VN",
                        KycDocumentType.NATIONAL_ID,
                        "fingerprint-1",
                        "9012",
                        "VN",
                        LocalDate.of(2032, 4, 12),
                        "1 Nguyen Hue",
                        "Ho Chi Minh City",
                        "VN"),
                NOW);
    }
}
