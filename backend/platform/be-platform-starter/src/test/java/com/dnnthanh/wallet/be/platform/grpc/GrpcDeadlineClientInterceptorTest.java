package com.dnnthanh.wallet.be.platform.grpc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GrpcDeadlineClientInterceptorTest {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void appliesDefaultDeadlineWhenCallerDidNotSetOne() {
        Channel channel = mock(Channel.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        var interceptor = new GrpcDeadlineClientInterceptor(Duration.ofSeconds(2));

        interceptor.interceptCall(method, CallOptions.DEFAULT, channel);

        var options = ArgumentCaptor.forClass(CallOptions.class);
        verify(channel).newCall(eq(method), options.capture());
        assertThat(options.getValue().getDeadline()).isNotNull();
        assertThat(options.getValue().getDeadline().timeRemaining(SECONDS)).isBetween(0L, 2L);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void preservesExplicitCallerDeadline() {
        Channel channel = mock(Channel.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        ClientCall call = mock(ClientCall.class);
        var explicit = CallOptions.DEFAULT.withDeadlineAfter(10, SECONDS);
        org.mockito.Mockito.when(channel.newCall(any(), any())).thenReturn(call);
        var interceptor = new GrpcDeadlineClientInterceptor(Duration.ofSeconds(2));

        interceptor.interceptCall(method, explicit, channel);

        var options = ArgumentCaptor.forClass(CallOptions.class);
        verify(channel).newCall(eq(method), options.capture());
        assertThat(options.getValue().getDeadline()).isSameAs(explicit.getDeadline());
    }
}
