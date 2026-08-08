package com.dnnthanh.wallet.be.kyc.adapter.in.web.mapper;

import com.dnnthanh.wallet.be.kyc.api.request.KycReviewDecisionRequest;
import com.dnnthanh.wallet.be.kyc.api.request.UpsertKycDraftRequest;
import com.dnnthanh.wallet.be.kyc.api.response.KycResponse;
import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;
import com.dnnthanh.wallet.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.wallet.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface KycApiMapper extends ModelResponseMapper<KycView, KycResponse> {
    @Override
    KycResponse modelToResponse(KycView model);

    UpsertKycDraftCommand draftRequestToCommand(UpsertKycDraftRequest request);

    KycReviewCommand reviewRequestToCommand(KycReviewDecisionRequest request);
}
