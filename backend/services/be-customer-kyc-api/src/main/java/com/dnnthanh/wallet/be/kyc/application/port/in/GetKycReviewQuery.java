package com.dnnthanh.wallet.be.kyc.application.port.in;

import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import java.util.UUID;

public interface GetKycReviewQuery {
    KycView getReview(UUID kycId);
}
