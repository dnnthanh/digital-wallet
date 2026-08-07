package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import com.dnnthanh.wallet.be.kyc.application.port.out.KycReviewAuditPort;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KycReviewAuditPersistenceAdapter implements KycReviewAuditPort {
    private static final String INSERT_SQL =
            """
            insert into kyc_review_audit
                (audit_id, kyc_id, reviewer_user_id, decision, rejection_reason_code, reviewed_at)
            values (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void appendReview(
            UUID kycId,
            String reviewerUserId,
            KycReviewDecision decision,
            String rejectionReasonCode,
            Instant reviewedAt) {
        jdbcTemplate.update(
                INSERT_SQL,
                UUID.randomUUID(),
                kycId,
                reviewerUserId,
                decision.name(),
                rejectionReasonCode,
                Timestamp.from(reviewedAt));
    }
}
