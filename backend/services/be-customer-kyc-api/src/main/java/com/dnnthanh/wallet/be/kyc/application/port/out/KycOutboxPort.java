package com.dnnthanh.wallet.be.kyc.application.port.out;

import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;

public interface KycOutboxPort {
    void appendStatusChanged(KycStatusChangedPayload payload);
}
