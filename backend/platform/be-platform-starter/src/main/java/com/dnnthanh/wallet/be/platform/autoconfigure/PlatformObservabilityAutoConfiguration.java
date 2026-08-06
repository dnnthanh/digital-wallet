package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.trace.PlatformKafkaObservationBeanPostProcessor;
import com.dnnthanh.wallet.be.platform.trace.TraceContextAccessor;
import com.dnnthanh.wallet.be.platform.trace.TraceHeaders;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.task.SimpleAsyncTaskExecutorCustomizer;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

@AutoConfiguration
public class PlatformObservabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    TraceContextAccessor traceContextAccessor(ObjectProvider<Tracer> tracerProvider) {
        return new TraceContextAccessor(tracerProvider);
    }

    @Bean
    RestClientCustomizer traceIdRestClientCustomizer(TraceContextAccessor traceContext) {
        return builder ->
                builder.requestInterceptor(
                        (request, body, execution) -> {
                            String traceId = traceContext.currentTraceId();
                            if (StringUtils.isNotBlank(traceId)) {
                                request.getHeaders().set(TraceHeaders.TRACE_ID, traceId);
                            }
                            return execution.execute(request, body);
                        });
    }

    @Bean
    static BeanPostProcessor platformKafkaObservationBeanPostProcessor(
            ObservationRegistry observationRegistry) {
        return new PlatformKafkaObservationBeanPostProcessor(observationRegistry);
    }

    @Bean
    SchedulingConfigurer platformSchedulingConfigurer(ObservationRegistry observationRegistry) {
        return registrar -> registrar.setObservationRegistry(observationRegistry);
    }

    @Bean
    ThreadPoolTaskExecutorCustomizer platformThreadPoolTaskExecutorCustomizer() {
        return executor -> executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
    }

    @Bean
    SimpleAsyncTaskExecutorCustomizer platformSimpleAsyncTaskExecutorCustomizer() {
        return executor -> executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
    }
}
