package com.dnnthanh.wallet.be.kyc.adapter.in.web;

import com.dnnthanh.wallet.be.kyc.adapter.in.web.mapper.KycApiMapper;
import com.dnnthanh.wallet.be.kyc.api.request.KycReviewDecisionRequest;
import com.dnnthanh.wallet.be.kyc.api.request.UpsertKycDraftRequest;
import com.dnnthanh.wallet.be.kyc.api.response.KycResponse;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetKycReviewQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetMyKycQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.ReviewKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.SubmitMyKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.UpsertMyKycDraftUseCase;
import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {
    private final GetMyKycQuery getMyKycQuery;
    private final UpsertMyKycDraftUseCase upsertMyKycDraftUseCase;
    private final SubmitMyKycUseCase submitMyKycUseCase;
    private final GetKycReviewQuery getKycReviewQuery;
    private final ReviewKycUseCase reviewKycUseCase;
    private final KycApiMapper mapper;

    @GetMapping("/me")
    @PreAuthorize("@walletAuthorization.hasPermission('KYC_SELF_READ')")
    public ApiResponse<KycResponse> getMyKyc() {
        return ApiResponse.success(mapper.modelToResponse(getMyKycQuery.getMyKyc()));
    }

    @PutMapping("/me/draft")
    @PreAuthorize("@walletAuthorization.hasPermission('KYC_SELF_WRITE')")
    public ApiResponse<KycResponse> upsertMyDraft(
            @Valid @RequestBody UpsertKycDraftRequest request) {
        return ApiResponse.success(
                mapper.modelToResponse(
                        upsertMyKycDraftUseCase.upsertMyDraft(
                                mapper.draftRequestToCommand(request))));
    }

    @PostMapping("/me/submit")
    @PreAuthorize("@walletAuthorization.hasPermission('KYC_SELF_SUBMIT')")
    public ApiResponse<KycResponse> submitMyKyc() {
        return ApiResponse.success(mapper.modelToResponse(submitMyKycUseCase.submitMyKyc()));
    }

    @GetMapping("/reviews/{kycId}")
    @PreAuthorize("@walletAuthorization.hasPermission('KYC_REVIEW')")
    public ApiResponse<KycResponse> getReview(@PathVariable UUID kycId) {
        return ApiResponse.success(mapper.modelToResponse(getKycReviewQuery.getReview(kycId)));
    }

    @PostMapping("/reviews/{kycId}/decision")
    @PreAuthorize("@walletAuthorization.hasPermission('KYC_REVIEW')")
    public ApiResponse<KycResponse> review(
            @PathVariable UUID kycId, @Valid @RequestBody KycReviewDecisionRequest request) {
        return ApiResponse.success(
                mapper.modelToResponse(
                        reviewKycUseCase.review(kycId, mapper.reviewRequestToCommand(request))));
    }
}
