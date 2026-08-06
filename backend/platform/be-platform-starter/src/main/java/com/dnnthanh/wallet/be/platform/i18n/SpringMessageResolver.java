package com.dnnthanh.wallet.be.platform.i18n;

import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringMessageResolver implements MessageResolver {
    private final MessageSource messageSource;

    @Override
    public String resolve(MessageSourceResolvable message) {
        return resolve(message, defaultLocale());
    }

    @Override
    public String resolve(MessageSourceResolvable message, Locale locale) {
        return messageSource.getMessage(
                message, Objects.requireNonNullElseGet(locale, this::defaultLocale));
    }

    @Override
    public String resolve(MessageSourceResolvable message, Object... arguments) {
        return resolve(message, defaultLocale(), arguments);
    }

    @Override
    public String resolve(MessageSourceResolvable message, Locale locale, Object... arguments) {
        Locale effectiveLocale = Objects.requireNonNullElseGet(locale, this::defaultLocale);
        Object[] effectiveArguments =
                Objects.requireNonNullElse(arguments, ArrayUtils.EMPTY_OBJECT_ARRAY);
        String[] codes = message.getCodes();
        String messageCode = ArrayUtils.isEmpty(codes) ? null : StringUtils.trimToNull(codes[0]);
        if (Objects.isNull(messageCode)) return message.getDefaultMessage();
        return messageSource.getMessage(
                messageCode, effectiveArguments, message.getDefaultMessage(), effectiveLocale);
    }

    private Locale defaultLocale() {
        return Objects.requireNonNullElse(LocaleContextHolder.getLocale(), Locale.getDefault());
    }
}
