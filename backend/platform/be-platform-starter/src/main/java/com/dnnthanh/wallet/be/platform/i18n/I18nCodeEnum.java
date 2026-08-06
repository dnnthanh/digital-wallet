package com.dnnthanh.wallet.be.platform.i18n;

import com.dnnthanh.wallet.be.platform.model.CodeEnum;

public interface I18nCodeEnum extends CodeEnum, MessageResolvable {
    Class<?> getDeclaringClass();

    @Override
    default String getMessageKey() {
        return I18nConstants.ENUM_PREFIX
                + I18nConstants.MESSAGE_KEY_SEPARATOR
                + getDeclaringClass().getSimpleName()
                + I18nConstants.MESSAGE_KEY_SEPARATOR
                + getCode();
    }

    @Override
    default String getDefaultMessage() {
        return getCode();
    }
}
