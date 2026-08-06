package com.dnnthanh.wallet.be.platform.exception;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.ERROR_CODE_REQUIRED;

import java.io.Serial;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public class BusinessException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;
    private final transient ErrorCode errorCode;
    private final transient Object[] messageArguments;
    private final transient Map<String, Object> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null, Map.of());
    }

    public BusinessException(ErrorCode errorCode, Object... messageArguments) {
        this(errorCode, errorCode.getCode(), messageArguments, Map.of(), null);
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        this(errorCode, detailMessage, null, Map.of(), null);
    }

    public BusinessException(
            ErrorCode errorCode, Object[] messageArguments, Map<String, Object> details) {
        this(errorCode, errorCode.getCode(), messageArguments, details, null);
    }

    public BusinessException(
            ErrorCode errorCode,
            String detailMessage,
            Object[] messageArguments,
            Map<String, Object> details,
            Throwable cause) {
        super(detailMessage, cause);
        this.errorCode = Objects.requireNonNull(errorCode, ERROR_CODE_REQUIRED);
        Object[] safeArguments =
                Objects.requireNonNullElse(messageArguments, ArrayUtils.EMPTY_OBJECT_ARRAY);
        this.messageArguments = Arrays.copyOf(safeArguments, safeArguments.length);
        this.details = Map.copyOf(MapUtils.emptyIfNull(details));
    }
}
