package com.dnnthanh.wallet.be.kyc.application.port.in;

import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;

public interface UpsertMyKycDraftUseCase {
    KycView upsertMyDraft(UpsertKycDraftCommand command);
}
