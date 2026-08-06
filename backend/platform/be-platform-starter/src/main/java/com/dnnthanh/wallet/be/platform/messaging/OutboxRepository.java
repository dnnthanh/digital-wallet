package com.dnnthanh.wallet.be.platform.messaging;

@FunctionalInterface
public interface OutboxRepository {
    void append(OutboxMessage message);
}
