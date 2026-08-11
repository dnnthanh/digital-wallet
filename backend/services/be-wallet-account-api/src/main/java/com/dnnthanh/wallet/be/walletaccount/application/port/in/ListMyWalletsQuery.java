package com.dnnthanh.wallet.be.walletaccount.application.port.in;

import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;
import java.util.List;

public interface ListMyWalletsQuery {
    List<WalletAccountView> listMyWallets();
}
