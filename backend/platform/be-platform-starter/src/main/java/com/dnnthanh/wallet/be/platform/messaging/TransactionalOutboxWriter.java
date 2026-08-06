package com.dnnthanh.wallet.be.platform.messaging;

import org.springframework.transaction.annotation.Transactional;

public final class TransactionalOutboxWriter {
    private final OutboxRepository repository;

    public TransactionalOutboxWriter(OutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void append(OutboxMessage message) {
        repository.append(message);
    }
}
