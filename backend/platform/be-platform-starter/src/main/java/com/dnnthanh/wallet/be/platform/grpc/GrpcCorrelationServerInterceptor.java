package com.dnnthanh.wallet.be.platform.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public final class GrpcCorrelationServerInterceptor implements ServerInterceptor {
    public static final Context.Key<String> CORRELATION_ID = Context.key("correlation-id");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String supplied = headers.get(GrpcCorrelationMetadata.CORRELATION_ID);
        String correlationId =
                StringUtils.defaultIfBlank(
                        StringUtils.trim(supplied), UUID.randomUUID().toString());
        return Contexts.interceptCall(
                Context.current().withValue(CORRELATION_ID, correlationId), call, headers, next);
    }
}
