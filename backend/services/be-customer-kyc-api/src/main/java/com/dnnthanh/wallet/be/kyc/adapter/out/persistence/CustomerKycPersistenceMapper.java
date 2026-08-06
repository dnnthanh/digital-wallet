package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.CustomerKycEntity;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import org.springframework.stereotype.Component;

@Component
public class CustomerKycPersistenceMapper {
    public CustomerKycEntity toEntity(CustomerKyc kyc) {
        CustomerKycEntity entity = new CustomerKycEntity();
        entity.setKycId(kyc.kycId());
        entity.setUserId(kyc.userId());
        entity.setScopePath(kyc.scopePath());
        entity.setLegalName(kyc.profile().legalName());
        entity.setDateOfBirth(kyc.profile().dateOfBirth());
        entity.setNationality(kyc.profile().nationality());
        entity.setDocumentType(kyc.profile().documentType());
        entity.setDocumentFingerprint(kyc.profile().documentFingerprint());
        entity.setDocumentLast4(kyc.profile().documentLast4());
        entity.setDocumentCountry(kyc.profile().documentCountry());
        entity.setDocumentExpiresAt(kyc.profile().documentExpiresAt());
        entity.setAddressLine1(kyc.profile().addressLine1());
        entity.setCity(kyc.profile().city());
        entity.setCountry(kyc.profile().country());
        entity.setStatus(kyc.status());
        entity.setSubmittedAt(kyc.submittedAt());
        entity.setReviewedAt(kyc.reviewedAt());
        entity.setReviewedBy(kyc.reviewedBy());
        entity.setRejectionReasonCode(kyc.rejectionReasonCode());
        entity.setCreatedAt(kyc.createdAt());
        entity.setUpdatedAt(kyc.updatedAt());
        entity.setVersion(kyc.version());
        return entity;
    }

    public CustomerKyc toDomain(CustomerKycEntity entity) {
        KycProfile profile =
                new KycProfile(
                        entity.getLegalName(),
                        entity.getDateOfBirth(),
                        entity.getNationality(),
                        entity.getDocumentType(),
                        entity.getDocumentFingerprint(),
                        entity.getDocumentLast4(),
                        entity.getDocumentCountry(),
                        entity.getDocumentExpiresAt(),
                        entity.getAddressLine1(),
                        entity.getCity(),
                        entity.getCountry());
        return new CustomerKyc(
                entity.getKycId(),
                entity.getUserId(),
                entity.getScopePath(),
                profile,
                entity.getStatus(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewedBy(),
                entity.getRejectionReasonCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
