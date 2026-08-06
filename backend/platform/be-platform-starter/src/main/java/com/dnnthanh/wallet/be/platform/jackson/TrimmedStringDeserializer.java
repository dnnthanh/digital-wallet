package com.dnnthanh.wallet.be.platform.jackson;

import java.util.Objects;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public final class TrimmedStringDeserializer extends ValueDeserializer<String> {
    private final boolean noTrim;

    public TrimmedStringDeserializer() {
        this(false);
    }

    private TrimmedStringDeserializer(boolean noTrim) {
        this.noTrim = noTrim;
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        String value = parser.getValueAsString();
        return Objects.isNull(value) || noTrim ? value : value.trim();
    }

    @Override
    public ValueDeserializer<?> createContextual(
            DeserializationContext context, BeanProperty property) {
        if (Objects.isNull(property)) return this;
        return new TrimmedStringDeserializer(Objects.nonNull(property.getAnnotation(NoTrim.class)));
    }
}
