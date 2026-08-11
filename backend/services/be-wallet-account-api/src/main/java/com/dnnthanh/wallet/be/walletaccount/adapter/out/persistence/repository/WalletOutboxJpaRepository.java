package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.repository;

import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.entity.WalletOutboxEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletOutboxJpaRepository extends JpaRepository<WalletOutboxEntity, UUID> {}
