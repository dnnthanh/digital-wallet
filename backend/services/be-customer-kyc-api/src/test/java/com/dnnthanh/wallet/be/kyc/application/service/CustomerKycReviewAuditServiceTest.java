package com.dnnthanh.wallet.be.kyc.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.DocumentFingerprintPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycAuthorizationPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycReviewAuditPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerKycReviewAuditServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");
    private static final String REVIEWER_ID = "reviewer-123";

    @Mock private CustomerKycRepositoryPort repository;
    @Mock private DocumentFingerprintPort fingerprintPort;
    @Mock private KycOutboxPort outboxPort;
    @Mock private KycReviewAuditPort reviewAuditPort;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private KycAuthorizationPort authorizationPort;

    private CustomerKycServiceImplement service;

    @BeforeEach
    void setUp() {
        service =
                new CustomerKycServiceImplement(
                        repository,
                        fingerprintPort,
                        outboxPort,
                        reviewAuditPort,
                        currentActorPort,
                        authorizationPort,
                        Clock.fixed(NOW, ZoneOffset.UTC));
        when(currentActorPort.userId()).thenReturn(REVIEWER_ID);
        when(authorizationPort.hasScope("/bank/demo-branch")).thenReturn(true);
    }

    @Test
    void successfulReviewAppendsImmutableAuditEvidence() {
        CustomerKyc pending = pending();
        when(repository.findById(pending.kycId())).thenReturn(Optional.of(pending));
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.review(
                pending.kycId(), new KycReviewCommand(KycReviewDecision.REJECT, "DOC_UNCLEAR"));

        verify(reviewAuditPort)
                .appendReview(
                        pending.kycId(), REVIEWER_ID, KycReviewDecision.REJECT, "DOC_UNCLEAR", NOW);
    }

    @Test
    void verifyDecisionDoesNotPersistRequestRejectionReason() {
        CustomerKyc pending = pending();
        when(repository.findById(pending.kycId())).thenReturn(Optional.of(pending));
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.review(
                pending.kycId(), new KycReviewCommand(KycReviewDecision.VERIFY, "IGNORED_REASON"));

        verify(reviewAuditPort)
                .appendReview(pending.kycId(), REVIEWER_ID, KycReviewDecision.VERIFY, null, NOW);
    }

    private static CustomerKyc pending() {
        return draft().submit(NOW.minusSeconds(60));
    }

    private static CustomerKyc draft() {
        return CustomerKyc.createDraft(
                UUID.randomUUID(),
                "user-123",
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
                NOW.minusSeconds(120));
    }
}
