package com.dnnthanh.wallet.be.kyc.api.response;

import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record KycResponse(
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
    public static KycResponse from(KycView view) {
        return new KycResponse(
                view.kycId(),
                view.userId(),
                view.scopePath(),
                view.legalName(),
                view.dateOfBirth(),
                view.nationality(),
                view.documentType(),
                view.documentLast4(),
                view.documentCountry(),
                view.documentExpiresAt(),
                view.addressLine1(),
                view.city(),
                view.country(),
                view.status(),
                view.submittedAt(),
                view.reviewedAt(),
                view.reviewedBy(),
                view.rejectionReasonCode(),
                view.createdAt(),
                view.updatedAt());
    }
}
