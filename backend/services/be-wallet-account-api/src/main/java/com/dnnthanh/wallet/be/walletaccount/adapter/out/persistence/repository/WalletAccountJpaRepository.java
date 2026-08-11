package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.repository;

import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.entity.WalletAccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletAccountJpaRepository extends JpaRepository<WalletAccountEntity, UUID> {
    Optional<WalletAccountEntity> findByWalletIdAndUserId(UUID walletId, String userId);

    List<WalletAccountEntity> findAllByUserIdOrderByCreatedAtAscWalletIdAsc(String userId);
}
