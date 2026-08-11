package com.dnnthanh.wallet.be.walletaccount.application.port.out;

import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletAccountRepositoryPort {
    Optional<WalletAccount> findByIdAndUserId(UUID walletId, String userId);

    List<WalletAccount> findAllByUserId(String userId);

    WalletAccount save(WalletAccount wallet);
}
