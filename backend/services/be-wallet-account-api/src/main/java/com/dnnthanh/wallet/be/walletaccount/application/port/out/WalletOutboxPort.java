package com.dnnthanh.wallet.be.walletaccount.application.port.out;

import com.dnnthanh.wallet.be.walletaccount.application.event.WalletAccountCreatedPayload;

public interface WalletOutboxPort {
    void appendCreated(WalletAccountCreatedPayload payload);
}
