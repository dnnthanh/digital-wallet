package com.dnnthanh.wallet.be.kyc.adapter.out.security;

import static com.dnnthanh.wallet.be.kyc.constant.KycInvariantMessages.DOCUMENT_FINGERPRINT_FAILED;
import static com.dnnthanh.wallet.be.kyc.constant.KycInvariantMessages.DOCUMENT_HMAC_SECRET_REQUIRED;
import static com.dnnthanh.wallet.be.kyc.constant.KycInvariantMessages.DOCUMENT_NUMBER_INVALID;
import static com.dnnthanh.wallet.be.kyc.constant.KycInvariantMessages.DOCUMENT_NUMBER_REQUIRED;

import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;
import com.dnnthanh.wallet.be.kyc.application.port.out.DocumentFingerprintPort;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class HmacDocumentFingerprintAdapter implements DocumentFingerprintPort {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int LAST_FOUR_LENGTH = 4;

    private final SecretKeySpec secretKey;

    public HmacDocumentFingerprintAdapter(String secret) {
        String requiredSecret = Validate.notBlank(secret, DOCUMENT_HMAC_SECRET_REQUIRED);
        this.secretKey =
                new SecretKeySpec(requiredSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    @Override
    public DocumentFingerprint fingerprint(String rawDocumentNumber) {
        String normalized = normalize(rawDocumentNumber);
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);
            String fingerprint =
                    Hex.encodeHexString(mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8)));
            int start = Math.max(0, normalized.length() - LAST_FOUR_LENGTH);
            return new DocumentFingerprint(fingerprint, normalized.substring(start));
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException(DOCUMENT_FINGERPRINT_FAILED, failure);
        }
    }

    private String normalize(String rawDocumentNumber) {
        String requiredDocumentNumber = Validate.notBlank(rawDocumentNumber, DOCUMENT_NUMBER_REQUIRED);
        String normalized =
                StringUtils.upperCase(requiredDocumentNumber, Locale.ROOT)
                        .replaceAll("[^A-Z0-9]", StringUtils.EMPTY);
        return Validate.notBlank(normalized, DOCUMENT_NUMBER_INVALID);
    }
}
