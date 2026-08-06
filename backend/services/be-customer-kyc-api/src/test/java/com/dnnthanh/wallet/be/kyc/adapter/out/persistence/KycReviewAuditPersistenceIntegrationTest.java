package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.kyc.CustomerKycApiApplication;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycReviewAuditPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
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
        classes = CustomerKycApiApplication.class,
        properties = {
            "wallet.kyc.security.document-hmac-secret=test-secret",
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
        })
class KycReviewAuditPersistenceIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private CustomerKycRepositoryPort repository;
    @Autowired private KycReviewAuditPort reviewAuditPort;
    @Autowired private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from kyc_review_audit");
        jdbcTemplate.update("delete from kyc_outbox_event");
        jdbcTemplate.update("delete from customer_kyc");
    }

    @Test
    void rejectedDraftEditDoesNotEraseHistoricalReviewEvidence() {
        CustomerKyc draft = repository.save(draft());
        CustomerKyc rejected =
                repository.save(
                        draft.submit(NOW.minusSeconds(60))
                                .review(
                                        KycReviewDecision.REJECT,
                                        "reviewer-123",
                                        "DOC_UNCLEAR",
                                        NOW.minusSeconds(30)));
        reviewAuditPort.appendReview(
                rejected.kycId(),
                "reviewer-123",
                KycReviewDecision.REJECT,
                "DOC_UNCLEAR",
                NOW.minusSeconds(30));

        repository.save(rejected.updateDraft(updatedProfile(), NOW));

        Map<String, Object> audit =
                jdbcTemplate.queryForMap(
                        "select reviewer_user_id, decision, rejection_reason_code from kyc_review_audit where kyc_id = ?",
                        rejected.kycId());
        assertThat(audit.get("reviewer_user_id")).isEqualTo("reviewer-123");
        assertThat(audit.get("decision")).isEqualTo("REJECT");
        assertThat(audit.get("rejection_reason_code")).isEqualTo("DOC_UNCLEAR");
    }

    private static CustomerKyc draft() {
        return CustomerKyc.createDraft(
                UUID.randomUUID(),
                "user-123",
                "/bank/demo-branch",
                profile("fingerprint-1", "9012"),
                NOW.minusSeconds(120));
    }

    private static KycProfile updatedProfile() {
        return profile("fingerprint-2", "3456");
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
