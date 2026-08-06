package com.dnnthanh.wallet.be.platform.exception;

import com.dnnthanh.wallet.be.platform.i18n.MessageResolvable;
import com.dnnthanh.wallet.be.platform.model.CodeEnum;
import org.springframework.http.HttpStatus;

public interface ErrorCode extends CodeEnum, MessageResolvable {
    @Override
    default String getDefaultMessage() {
        return getCode();
    }

    HttpStatus getHttpStatus();
}
