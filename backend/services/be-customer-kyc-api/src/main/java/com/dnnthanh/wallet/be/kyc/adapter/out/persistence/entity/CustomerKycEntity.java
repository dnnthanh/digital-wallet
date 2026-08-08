package com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity;

import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_kyc")
@Getter
@Setter
@NoArgsConstructor
public class CustomerKycEntity {
    @Id
    @Column(name = "kyc_id", nullable = false, updatable = false)
    private UUID kycId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "scope_path", nullable = false, updatable = false, length = 512)
    private String scopePath;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "nationality", nullable = false, length = 2)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 32)
    private KycDocumentType documentType;

    @Column(name = "document_fingerprint", nullable = false, length = 64)
    private String documentFingerprint;

    @Column(name = "document_last4", nullable = false, length = 4)
    private String documentLast4;

    @Column(name = "document_country", nullable = false, length = 2)
    private String documentCountry;

    @Column(name = "document_expires_at", nullable = false)
    private LocalDate documentExpiresAt;

    @Column(name = "address_line1", nullable = false, length = 512)
    private String addressLine1;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private KycStatus status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "rejection_reason_code", length = 128)
    private String rejectionReasonCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
