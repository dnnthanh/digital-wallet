package com.dnnthanh.wallet.be.platform.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

class GrpcCorrelationInterceptorTest {
    @Test
    void usesStableAsciiMetadataKey() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcCorrelationMetadata.CORRELATION_ID, "corr-123");
        assertThat(metadata.get(GrpcCorrelationMetadata.CORRELATION_ID)).isEqualTo("corr-123");
    }
}
