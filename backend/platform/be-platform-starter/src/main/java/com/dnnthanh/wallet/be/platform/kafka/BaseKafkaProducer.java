package com.dnnthanh.wallet.be.platform.kafka;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_TEMPLATE_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_TOPIC_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_VALUE_REQUIRED;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Slf4j
public abstract class BaseKafkaProducer<T> {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected BaseKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, KAFKA_TEMPLATE_REQUIRED);
    }

    protected final CompletableFuture<SendResult<String, Object>> send(
            String topic, String key, T value) {
        Objects.requireNonNull(topic, KAFKA_TOPIC_REQUIRED);
        Objects.requireNonNull(value, KAFKA_VALUE_REQUIRED);
        return kafkaTemplate
                .send(topic, key, value)
                .whenComplete(
                        (result, failure) -> {
                            if (Objects.nonNull(failure))
                                log.warn(
                                        "kafka_publish_failed topic={} keyHash={} valueType={} failure={}",
                                        topic,
                                        Objects.hashCode(key),
                                        value.getClass().getSimpleName(),
                                        failure.toString());
                        });
    }
}
