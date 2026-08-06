package com.dnnthanh.wallet.be.platform.grpc;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.GRPC_DEFAULT_DEADLINE_POSITIVE;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.GRPC_DEFAULT_DEADLINE_REQUIRED;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;

public final class GrpcDeadlineClientInterceptor implements ClientInterceptor {
    private final Duration defaultDeadline;

    public GrpcDeadlineClientInterceptor(Duration defaultDeadline) {
        Validate.notNull(defaultDeadline, GRPC_DEFAULT_DEADLINE_REQUIRED);
        Validate.isTrue(
                !defaultDeadline.isZero() && !defaultDeadline.isNegative(),
                GRPC_DEFAULT_DEADLINE_POSITIVE);
        this.defaultDeadline = defaultDeadline;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        CallOptions effective =
                Objects.isNull(callOptions.getDeadline())
                        ? callOptions.withDeadlineAfter(
                                defaultDeadline.toNanos(), TimeUnit.NANOSECONDS)
                        : callOptions;
        return next.newCall(method, effective);
    }
}
