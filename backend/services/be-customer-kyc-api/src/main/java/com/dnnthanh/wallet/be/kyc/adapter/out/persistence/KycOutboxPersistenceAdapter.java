package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import static com.dnnthanh.wallet.be.kyc.constant.KycEventTypes.CUSTOMER_KYC_STATUS_CHANGED;

import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KycOutboxPersistenceAdapter implements KycOutboxPort {
    private static final String INSERT_SQL =
            """
            insert into kyc_outbox_event
                (event_id, aggregate_id, event_type, payload, created_at, attempt_count)
            values (?, ?, ?, cast(? as jsonb), ?, 0)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final OutboxPayloadCodec payloadCodec;

    @Override
    public void appendStatusChanged(KycStatusChangedPayload payload) {
        jdbcTemplate.update(
                INSERT_SQL,
                UUID.randomUUID(),
                payload.kycId(),
                CUSTOMER_KYC_STATUS_CHANGED,
                payloadCodec.write(payload),
                payload.changedAt());
    }
}
