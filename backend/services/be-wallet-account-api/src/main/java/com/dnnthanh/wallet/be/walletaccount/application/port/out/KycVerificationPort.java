package com.dnnthanh.wallet.be.walletaccount.application.port.out;

import com.dnnthanh.wallet.be.walletaccount.application.model.VerifiedKyc;

public interface KycVerificationPort {
    VerifiedKyc requireVerified(String expectedUserId);
}
