package com.dnnthanh.wallet.be.platform.exception;

public final class ServiceTokenAcquisitionException extends BusinessException {
    public ServiceTokenAcquisitionException(String diagnosticMessage) {
        super(PlatformErrorCode.SERVICE_TOKEN_ACQUISITION_FAILED, diagnosticMessage);
    }

    public ServiceTokenAcquisitionException(String diagnosticMessage, Throwable cause) {
        super(
                PlatformErrorCode.SERVICE_TOKEN_ACQUISITION_FAILED,
                diagnosticMessage,
                null,
                null,
                cause);
    }
}
