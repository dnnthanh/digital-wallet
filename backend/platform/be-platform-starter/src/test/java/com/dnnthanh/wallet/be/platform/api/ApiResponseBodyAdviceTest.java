package com.dnnthanh.wallet.be.platform.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

class ApiResponseBodyAdviceTest {
    @Test
    void shouldBypassEnvelopeForRawResponseMethods() throws Exception {
        ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();
        Method method = RawController.class.getDeclaredMethod("raw");
        MethodParameter returnType = new MethodParameter(method, -1);

        assertThat(advice.supports(returnType, MappingJackson2HttpMessageConverter.class)).isFalse();
    }

    private static final class RawController {
        @RawResponse
        Map<String, Object> raw() {
            return Map.of("keys", "public-only");
        }
    }
}
