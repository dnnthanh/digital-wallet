package com.dnnthanh.wallet.be.walletaccount.application.service;

import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;

public interface WalletAccountWriteTransaction {
    WalletAccount open(String userId, String scopePath, String currency);
}
