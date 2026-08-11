package com.dnnthanh.wallet.be.walletaccount.application.port.in;

import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;
import java.util.UUID;

public interface GetMyWalletQuery {
    WalletAccountView getMyWallet(UUID walletId);
}
