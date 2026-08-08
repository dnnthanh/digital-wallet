package com.dnnthanh.wallet.be.platform.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.task.SimpleAsyncTaskExecutorCustomizer;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

class PlatformObservabilityAutoConfigurationTest {
    private final PlatformObservabilityAutoConfiguration configuration =
            new PlatformObservabilityAutoConfiguration();
    private final ObservationRegistry observationRegistry = ObservationRegistry.create();

    @Test
    void enablesObservationForKafkaTemplatesAndListenerFactories() {
        BeanPostProcessor postProcessor =
                configuration.platformKafkaObservationBeanPostProcessor(observationRegistry);
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);

        postProcessor.postProcessBeforeInitialization(kafkaTemplate, "kafkaTemplate");

        verify(kafkaTemplate).setObservationEnabled(true);
        verify(kafkaTemplate).setObservationRegistry(observationRegistry);

        var listenerFactory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        postProcessor.postProcessBeforeInitialization(
                listenerFactory, "kafkaListenerContainerFactory");

        assertThat(listenerFactory.getContainerProperties().isObservationEnabled()).isTrue();
        assertThat(listenerFactory.getContainerProperties().getObservationRegistry())
                .isSameAs(observationRegistry);
        assertThat(listenerFactory.getContainerProperties().isRecordObservationsInBatch()).isTrue();
    }

    @Test
    void registersObservationRegistryForScheduledJobs() {
        SchedulingConfigurer configurer =
                configuration.platformSchedulingConfigurer(observationRegistry);
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();

        configurer.configureTasks(registrar);

        assertThat(registrar.getObservationRegistry()).isSameAs(observationRegistry);
    }

    @Test
    void installsContextPropagationOnBootManagedAsyncExecutors() {
        ThreadPoolTaskExecutorCustomizer threadPoolCustomizer =
                configuration.platformThreadPoolTaskExecutorCustomizer();
        ThreadPoolTaskExecutor threadPoolExecutor = mock(ThreadPoolTaskExecutor.class);
        threadPoolCustomizer.customize(threadPoolExecutor);
        verify(threadPoolExecutor).setTaskDecorator(isA(ContextPropagatingTaskDecorator.class));

        SimpleAsyncTaskExecutorCustomizer simpleCustomizer =
                configuration.platformSimpleAsyncTaskExecutorCustomizer();
        SimpleAsyncTaskExecutor simpleExecutor = mock(SimpleAsyncTaskExecutor.class);
        simpleCustomizer.customize(simpleExecutor);
        verify(simpleExecutor).setTaskDecorator(isA(ContextPropagatingTaskDecorator.class));
    }

    @Test
    void isDiscoverableByComponentScanning() {
        assertThat(PlatformObservabilityAutoConfiguration.class).hasAnnotation(Configuration.class);
    }
}
