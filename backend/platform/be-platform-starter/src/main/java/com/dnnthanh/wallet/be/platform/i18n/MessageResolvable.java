package com.dnnthanh.wallet.be.platform.i18n;

import org.apache.commons.lang3.ArrayUtils;

public interface MessageResolvable extends org.springframework.context.MessageSourceResolvable {
    String getMessageKey();

    @Override
    default String[] getCodes() {
        return new String[] {getMessageKey()};
    }

    @Override
    default Object[] getArguments() {
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    @Override
    default String getDefaultMessage() {
        return getMessageKey();
    }
}
