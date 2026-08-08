package com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity;

import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kyc_review_audit")
@Getter
@Setter
@NoArgsConstructor
public class KycReviewAuditEntity {
    @Id
    @Column(name = "audit_id", nullable = false, updatable = false)
    private UUID auditId;

    @Column(name = "kyc_id", nullable = false, updatable = false)
    private UUID kycId;

    @Column(name = "reviewer_user_id", nullable = false, updatable = false)
    private String reviewerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, updatable = false)
    private KycReviewDecision decision;

    @Column(name = "rejection_reason_code", updatable = false)
    private String rejectionReasonCode;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;
}
