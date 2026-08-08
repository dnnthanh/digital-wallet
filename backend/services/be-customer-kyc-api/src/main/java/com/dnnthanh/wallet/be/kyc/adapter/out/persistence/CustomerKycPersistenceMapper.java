package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.CustomerKycEntity;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.platform.mapping.ModelEntityMapper;
import com.dnnthanh.wallet.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public interface CustomerKycPersistenceMapper
        extends ModelEntityMapper<CustomerKyc, CustomerKycEntity> {
    @Override
    @Mapping(target = "legalName", source = "profile.legalName")
    @Mapping(target = "dateOfBirth", source = "profile.dateOfBirth")
    @Mapping(target = "nationality", source = "profile.nationality")
    @Mapping(target = "documentType", source = "profile.documentType")
    @Mapping(target = "documentFingerprint", source = "profile.documentFingerprint")
    @Mapping(target = "documentLast4", source = "profile.documentLast4")
    @Mapping(target = "documentCountry", source = "profile.documentCountry")
    @Mapping(target = "documentExpiresAt", source = "profile.documentExpiresAt")
    @Mapping(target = "addressLine1", source = "profile.addressLine1")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "country", source = "profile.country")
    CustomerKycEntity modelToEntity(CustomerKyc model);

    @Override
    @Mapping(target = "profile", source = "entity")
    CustomerKyc entityToModel(CustomerKycEntity entity);

    KycProfile entityToProfile(CustomerKycEntity entity);
}
