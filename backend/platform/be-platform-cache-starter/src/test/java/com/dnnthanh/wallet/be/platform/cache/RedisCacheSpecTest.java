package com.dnnthanh.wallet.be.platform.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RedisCacheSpecTest {
    @Test
    void requiresNamePositiveTtlAndValueType() {
        var spec = new RedisCacheSpec("permissions", Duration.ofMinutes(5), SampleValue.class);
        assertThat(spec.name()).isEqualTo("permissions");
        assertThat(spec.ttl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(spec.valueType()).isEqualTo(SampleValue.class);
        assertThatThrownBy(() -> new RedisCacheSpec("", Duration.ofMinutes(1), SampleValue.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RedisCacheSpec("x", Duration.ZERO, SampleValue.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    record SampleValue(String value) {}
}
