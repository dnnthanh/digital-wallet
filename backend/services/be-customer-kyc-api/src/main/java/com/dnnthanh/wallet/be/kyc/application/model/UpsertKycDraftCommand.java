package com.dnnthanh.wallet.be.kyc.application.model;

import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import java.time.LocalDate;

public record UpsertKycDraftCommand(
        String legalName,
        LocalDate dateOfBirth,
        String nationality,
        KycDocumentType documentType,
        String documentNumber,
        String documentCountry,
        LocalDate documentExpiresAt,
        String addressLine1,
        String city,
        String country) {}
