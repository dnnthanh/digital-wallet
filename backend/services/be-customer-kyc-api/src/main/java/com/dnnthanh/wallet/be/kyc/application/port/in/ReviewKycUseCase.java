package com.dnnthanh.wallet.be.kyc.application.port.in;

import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import java.util.UUID;

public interface ReviewKycUseCase {
    KycView review(UUID kycId, KycReviewCommand command);
}
