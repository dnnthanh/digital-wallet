package com.dnnthanh.wallet.be.kyc.application.event;

import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import java.time.Instant;
import java.util.UUID;

public record KycStatusChangedPayload(
        UUID kycId,
        String userId,
        KycStatus previousStatus,
        KycStatus currentStatus,
        Instant changedAt) {}
