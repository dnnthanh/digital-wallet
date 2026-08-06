package com.dnnthanh.wallet.be.platform.i18n;

import java.util.Locale;
import org.springframework.context.MessageSourceResolvable;

public interface MessageResolver {
    String resolve(MessageSourceResolvable message);

    String resolve(MessageSourceResolvable message, Locale locale);

    String resolve(MessageSourceResolvable message, Object... arguments);

    String resolve(MessageSourceResolvable message, Locale locale, Object... arguments);
}
