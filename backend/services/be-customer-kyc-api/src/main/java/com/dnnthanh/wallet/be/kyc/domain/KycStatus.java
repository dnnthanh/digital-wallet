package com.dnnthanh.wallet.be.kyc.domain;

import com.dnnthanh.wallet.be.platform.model.CodeEnum;

public enum KycStatus implements CodeEnum {
    DRAFT,
    PENDING_REVIEW,
    VERIFIED,
    REJECTED
}
