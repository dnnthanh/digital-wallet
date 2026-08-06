package com.dnnthanh.wallet.be.kyc.application.model;

import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
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
        Instant updatedAt) {
    public static KycView from(CustomerKyc kyc) {
        return new KycView(
                kyc.kycId(),
                kyc.userId(),
                kyc.scopePath(),
                kyc.profile().legalName(),
                kyc.profile().dateOfBirth(),
                kyc.profile().nationality(),
                kyc.profile().documentType(),
                kyc.profile().documentLast4(),
                kyc.profile().documentCountry(),
                kyc.profile().documentExpiresAt(),
                kyc.profile().addressLine1(),
                kyc.profile().city(),
                kyc.profile().country(),
                kyc.status(),
                kyc.submittedAt(),
                kyc.reviewedAt(),
                kyc.reviewedBy(),
                kyc.rejectionReasonCode(),
                kyc.createdAt(),
                kyc.updatedAt());
    }
}
