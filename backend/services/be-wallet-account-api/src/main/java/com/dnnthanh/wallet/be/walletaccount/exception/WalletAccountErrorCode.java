package com.dnnthanh.wallet.be.walletaccount.exception;

import com.dnnthanh.wallet.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WalletAccountErrorCode implements ErrorCode {
    WALLET_NOT_FOUND("WALLET_NOT_FOUND", "error.wallet.not-found", HttpStatus.NOT_FOUND),
    WALLET_KYC_REQUIRED("WALLET_KYC_REQUIRED", "error.wallet.kyc-required", HttpStatus.CONFLICT),
    WALLET_KYC_UNAVAILABLE(
            "WALLET_KYC_UNAVAILABLE",
            "error.wallet.kyc-unavailable",
            HttpStatus.SERVICE_UNAVAILABLE),
    WALLET_AUTHORIZATION_UNAVAILABLE(
            "WALLET_AUTHORIZATION_UNAVAILABLE",
            "error.wallet.authorization-unavailable",
            HttpStatus.SERVICE_UNAVAILABLE),
    WALLET_UNSUPPORTED_CURRENCY(
            "WALLET_UNSUPPORTED_CURRENCY",
            "error.wallet.unsupported-currency",
            HttpStatus.BAD_REQUEST),
    WALLET_ALREADY_EXISTS(
            "WALLET_ALREADY_EXISTS", "error.wallet.already-exists", HttpStatus.CONFLICT);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
