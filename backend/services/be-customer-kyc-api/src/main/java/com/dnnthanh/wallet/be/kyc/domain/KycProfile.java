package com.dnnthanh.wallet.be.kyc.domain;

import java.time.LocalDate;

public record KycProfile(
        String legalName,
        LocalDate dateOfBirth,
        String nationality,
        KycDocumentType documentType,
        String documentFingerprint,
        String documentLast4,
        String documentCountry,
        LocalDate documentExpiresAt,
        String addressLine1,
        String city,
        String country) {}
