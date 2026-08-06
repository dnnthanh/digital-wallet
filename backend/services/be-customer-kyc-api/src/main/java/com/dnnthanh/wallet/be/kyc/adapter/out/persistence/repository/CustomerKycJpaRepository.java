package com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.CustomerKycEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerKycJpaRepository extends JpaRepository<CustomerKycEntity, UUID> {
    Optional<CustomerKycEntity> findByUserId(String userId);
}
