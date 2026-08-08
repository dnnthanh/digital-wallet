package com.dnnthanh.wallet.be.platform.kafka;

import com.dnnthanh.wallet.be.platform.event.EventEnvelope;
import com.dnnthanh.wallet.be.platform.stereotype.Adapter;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Adapter
public class DomainEventProducer extends BaseKafkaProducer<EventEnvelope<?>> {
    public DomainEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public CompletableFuture<SendResult<String, Object>> publish(
            String topic, String key, EventEnvelope<?> event) {
        return send(topic, key, event);
    }
}
