package com.dnnthanh.wallet.be.kyc.application.mapper;

import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public interface CustomerKycApplicationMapper {
    @Mapping(target = "documentFingerprint", source = "fingerprint.fingerprint")
    @Mapping(target = "documentLast4", source = "fingerprint.last4")
    KycProfile toProfile(UpsertKycDraftCommand command, DocumentFingerprint fingerprint);

    @Mapping(target = "legalName", source = "profile.legalName")
    @Mapping(target = "dateOfBirth", source = "profile.dateOfBirth")
    @Mapping(target = "nationality", source = "profile.nationality")
    @Mapping(target = "documentType", source = "profile.documentType")
    @Mapping(target = "documentLast4", source = "profile.documentLast4")
    @Mapping(target = "documentCountry", source = "profile.documentCountry")
    @Mapping(target = "documentExpiresAt", source = "profile.documentExpiresAt")
    @Mapping(target = "addressLine1", source = "profile.addressLine1")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "country", source = "profile.country")
    KycView toView(CustomerKyc kyc);
}
