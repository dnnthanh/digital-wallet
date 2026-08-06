package com.dnnthanh.wallet.be.platform.trace;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OBSERVATION_REGISTRY_REQUIRED;

import io.micrometer.observation.ObservationRegistry;
import java.util.Objects;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public final class PlatformKafkaObservationBeanPostProcessor implements BeanPostProcessor {
    private final ObservationRegistry observationRegistry;

    public PlatformKafkaObservationBeanPostProcessor(ObservationRegistry observationRegistry) {
        this.observationRegistry =
                Objects.requireNonNull(observationRegistry, OBSERVATION_REGISTRY_REQUIRED);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof KafkaTemplate<?, ?> kafkaTemplate) {
            kafkaTemplate.setObservationEnabled(true);
            kafkaTemplate.setObservationRegistry(observationRegistry);
        }

        if (bean instanceof AbstractKafkaListenerContainerFactory<?, ?, ?> listenerFactory) {
            var properties = listenerFactory.getContainerProperties();
            properties.setObservationEnabled(true);
            properties.setObservationRegistry(observationRegistry);
            properties.setRecordObservationsInBatch(true);
        }

        return bean;
    }
}
