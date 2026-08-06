package com.dnnthanh.wallet.be.kyc.api.request;

import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpsertKycDraftRequest(
        @NotBlank @Size(max = 255) String legalName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String nationality,
        @NotNull KycDocumentType documentType,
        @NotBlank @Size(min = 4, max = 64) String documentNumber,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String documentCountry,
        @NotNull @Future LocalDate documentExpiresAt,
        @NotBlank @Size(max = 512) String addressLine1,
        @NotBlank @Size(max = 255) String city,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String country) {
    public UpsertKycDraftCommand toCommand() {
        return new UpsertKycDraftCommand(
                legalName,
                dateOfBirth,
                nationality,
                documentType,
                documentNumber,
                documentCountry,
                documentExpiresAt,
                addressLine1,
                city,
                country);
    }
}
