package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.CustomerKycEntity;
import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository.CustomerKycJpaRepository;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerKycPersistenceAdapter implements CustomerKycRepositoryPort {
    private static final String DOCUMENT_FINGERPRINT_CONSTRAINT =
            "uk_customer_kyc_document_fingerprint";

    private final CustomerKycJpaRepository repository;
    private final CustomerKycPersistenceMapper mapper;

    @Override
    public Optional<CustomerKyc> findByUserId(String userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<CustomerKyc> findById(UUID kycId) {
        return repository.findById(kycId).map(mapper::toDomain);
    }

    @Override
    public CustomerKyc save(CustomerKyc kyc) {
        try {
            CustomerKycEntity saved = repository.saveAndFlush(mapper.toEntity(kyc));
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException failure) {
            if (isDocumentFingerprintConflict(failure)) {
                throw new BusinessException(KycErrorCode.KYC_DOCUMENT_ALREADY_EXISTS);
            }
            throw failure;
        }
    }

    private boolean isDocumentFingerprintConflict(DataIntegrityViolationException failure) {
        return ExceptionUtils.getThrowableList(failure).stream()
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast)
                .map(ConstraintViolationException::getConstraintName)
                .anyMatch(DOCUMENT_FINGERPRINT_CONSTRAINT::equals);
    }
}
