package com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.KycOutboxEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycOutboxJpaRepository extends JpaRepository<KycOutboxEntity, UUID> {}
