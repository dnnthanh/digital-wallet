package com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.KycReviewAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycReviewAuditJpaRepository extends JpaRepository<KycReviewAuditEntity, UUID> {}
