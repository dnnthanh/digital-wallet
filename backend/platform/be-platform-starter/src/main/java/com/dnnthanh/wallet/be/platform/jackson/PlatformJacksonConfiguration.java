package com.dnnthanh.wallet.be.platform.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class PlatformJacksonConfiguration {
    @Bean
    public JsonMapperBuilderCustomizer nonNullPropertyCustomizer() {
        return builder ->
                builder.changeDefaultPropertyInclusion(
                        inclusion ->
                                inclusion
                                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                                        .withContentInclusion(JsonInclude.Include.NON_NULL));
    }

    @Bean
    public JsonMapperBuilderCustomizer stringTrimmingCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("wallet-string-trimming");
            module.addDeserializer(String.class, new TrimmedStringDeserializer());
            builder.addModule(module);
        };
    }
}
