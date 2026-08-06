package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.kyc.CustomerKycApiApplication;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
class CustomerKycPersistenceAdapterIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private CustomerKycRepositoryPort repository;
    @Autowired private JdbcTemplate jdbcTemplate;

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
    void liquibaseCreatesCustomerKycTable() {
        Integer tableCount =
                jdbcTemplate.queryForObject(
                        "select count(*) from information_schema.tables where table_name = 'customer_kyc'",
                        Integer.class);

        assertThat(tableCount).isEqualTo(1);
    }

    @Test
    void savesAndLoadsKycWithoutRawDocumentNumber() {
        CustomerKyc saved = repository.save(draft("user-1", "fingerprint-1"));

        CustomerKyc loaded = repository.findById(saved.kycId()).orElseThrow();

        assertThat(loaded.userId()).isEqualTo("user-1");
        assertThat(loaded.profile().documentFingerprint()).isEqualTo("fingerprint-1");
        assertThat(loaded.profile().documentLast4()).isEqualTo("9012");
        assertThat(loaded.version()).isGreaterThanOrEqualTo(0L);
        assertThat(columnNames()).doesNotContain("document_number");
    }

    @Test
    void duplicateDocumentFingerprintMapsToStableConflict() {
        repository.save(draft("user-1", "fingerprint-1"));

        assertThatThrownBy(() -> repository.save(draft("user-2", "fingerprint-1")))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_DOCUMENT_ALREADY_EXISTS));
    }

    @Test
    void staleVersionCannotOverwriteNewerDraft() {
        CustomerKyc saved = repository.save(draft("user-1", "fingerprint-1"));
        CustomerKyc first = repository.findById(saved.kycId()).orElseThrow();
        CustomerKyc stale = repository.findById(saved.kycId()).orElseThrow();

        repository.save(first.updateDraft(profile("fingerprint-2", "2222"), NOW.plusSeconds(10)));

        assertThatThrownBy(
                        () ->
                                repository.save(
                                        stale.updateDraft(
                                                profile("fingerprint-3", "3333"),
                                                NOW.plusSeconds(20))))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    private java.util.List<String> columnNames() {
        return jdbcTemplate.queryForList(
                "select column_name from information_schema.columns where table_name = 'customer_kyc'",
                String.class);
    }

    private static CustomerKyc draft(String userId, String fingerprint) {
        return CustomerKyc.createDraft(
                UUID.randomUUID(), userId, "/bank/demo-branch", profile(fingerprint, "9012"), NOW);
    }

    private static KycProfile profile(String fingerprint, String last4) {
        return new KycProfile(
                "Nguyen Van A",
                LocalDate.of(1998, 4, 12),
                "VN",
                KycDocumentType.NATIONAL_ID,
                fingerprint,
                last4,
                "VN",
                LocalDate.of(2032, 4, 12),
                "1 Nguyen Hue",
                "Ho Chi Minh City",
                "VN");
    }
}
