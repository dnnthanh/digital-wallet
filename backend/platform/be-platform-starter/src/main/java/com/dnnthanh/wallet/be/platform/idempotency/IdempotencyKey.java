package com.dnnthanh.wallet.be.platform.idempotency;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.exception.PlatformErrorCode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public record IdempotencyKey(String value) {
    public static final int MAX_LENGTH = 128;

    public IdempotencyKey {
        value = StringUtils.trimToNull(value);
        if (Objects.isNull(value) || value.length() > MAX_LENGTH) {
            throw new BusinessException(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }
}
