package com.dnnthanh.wallet.be.kyc.application.port.out;

import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;

public interface DocumentFingerprintPort {
    DocumentFingerprint fingerprint(String rawDocumentNumber);
}
