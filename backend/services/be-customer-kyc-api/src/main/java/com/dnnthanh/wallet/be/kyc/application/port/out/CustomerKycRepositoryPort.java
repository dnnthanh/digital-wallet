package com.dnnthanh.wallet.be.kyc.application.port.out;

import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import java.util.Optional;
import java.util.UUID;

public interface CustomerKycRepositoryPort {
    Optional<CustomerKyc> findByUserId(String userId);

    Optional<CustomerKyc> findById(UUID kycId);

    CustomerKyc save(CustomerKyc kyc);
}
