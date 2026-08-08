package com.dnnthanh.wallet.be.kyc.application.model;

import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record KycView(
        UUID kycId,
        String userId,
        String scopePath,
        String legalName,
        LocalDate dateOfBirth,
        String nationality,
        KycDocumentType documentType,
        String documentLast4,
        String documentCountry,
        LocalDate documentExpiresAt,
        String addressLine1,
        String city,
        String country,
        KycStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewedBy,
        String rejectionReasonCode,
        Instant createdAt,
        Instant updatedAt) {}
