package com.dnnthanh.wallet.be.walletaccount.application.port.in;

import com.dnnthanh.wallet.be.walletaccount.application.model.OpenWalletCommand;
import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;

public interface OpenWalletAccountUseCase {
    WalletAccountView open(OpenWalletCommand command);
}
