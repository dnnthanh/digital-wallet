package com.dnnthanh.wallet.be.platform.grpc;

import io.grpc.Metadata;

public final class GrpcCorrelationMetadata {
    public static final Metadata.Key<String> CORRELATION_ID =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    private GrpcCorrelationMetadata() {}
}
