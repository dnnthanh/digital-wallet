package com.dnnthanh.wallet.be.kyc.exception;

import com.dnnthanh.wallet.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KycErrorCode implements ErrorCode {
    KYC_NOT_FOUND("KYC_NOT_FOUND", "error.kyc.not-found", HttpStatus.NOT_FOUND),
    KYC_NOT_EDITABLE("KYC_NOT_EDITABLE", "error.kyc.not-editable", HttpStatus.CONFLICT),
    KYC_NOT_SUBMITTABLE("KYC_NOT_SUBMITTABLE", "error.kyc.not-submittable", HttpStatus.CONFLICT),
    KYC_NOT_REVIEWABLE("KYC_NOT_REVIEWABLE", "error.kyc.not-reviewable", HttpStatus.CONFLICT),
    KYC_REJECTION_REASON_REQUIRED(
            "KYC_REJECTION_REASON_REQUIRED",
            "error.kyc.rejection-reason-required",
            HttpStatus.BAD_REQUEST),
    KYC_SELF_REVIEW_FORBIDDEN(
            "KYC_SELF_REVIEW_FORBIDDEN", "error.kyc.self-review-forbidden", HttpStatus.FORBIDDEN),
    KYC_SCOPE_REQUIRED("KYC_SCOPE_REQUIRED", "error.kyc.scope-required", HttpStatus.FORBIDDEN),
    KYC_AUTHORIZATION_UNAVAILABLE(
            "KYC_AUTHORIZATION_UNAVAILABLE",
            "error.kyc.authorization-unavailable",
            HttpStatus.SERVICE_UNAVAILABLE),
    KYC_DOCUMENT_ALREADY_EXISTS(
            "KYC_DOCUMENT_ALREADY_EXISTS",
            "error.kyc.document-already-exists",
            HttpStatus.CONFLICT),
    KYC_CONCURRENT_MODIFICATION(
            "KYC_CONCURRENT_MODIFICATION",
            "error.kyc.concurrent-modification",
            HttpStatus.CONFLICT);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
