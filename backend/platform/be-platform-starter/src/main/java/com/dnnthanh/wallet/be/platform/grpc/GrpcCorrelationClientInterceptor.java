package com.dnnthanh.wallet.be.platform.grpc;

import com.dnnthanh.wallet.be.platform.web.CorrelationContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public final class GrpcCorrelationClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<>(delegate) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                CorrelationContext.currentId()
                        .ifPresent(id -> headers.put(GrpcCorrelationMetadata.CORRELATION_ID, id));
                super.start(responseListener, headers);
            }
        };
    }
}
