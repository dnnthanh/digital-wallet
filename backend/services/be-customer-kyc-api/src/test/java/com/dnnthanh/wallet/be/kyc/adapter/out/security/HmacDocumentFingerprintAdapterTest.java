package com.dnnthanh.wallet.be.kyc.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;
import org.junit.jupiter.api.Test;

class HmacDocumentFingerprintAdapterTest {
    @Test
    void sameNormalizedDocumentAndSecretProduceSameFingerprint() {
        HmacDocumentFingerprintAdapter adapter =
                new HmacDocumentFingerprintAdapter("test-secret-a");

        DocumentFingerprint first = adapter.fingerprint(" ab-12 34 ");
        DocumentFingerprint second = adapter.fingerprint("AB1234");

        assertThat(first.fingerprint()).isEqualTo(second.fingerprint());
        assertThat(first.last4()).isEqualTo("1234");
        assertThat(first.fingerprint()).hasSize(64);
        assertThat(first.fingerprint()).doesNotContain("AB1234");
    }

    @Test
    void differentSecretsProduceDifferentFingerprints() {
        DocumentFingerprint first =
                new HmacDocumentFingerprintAdapter("test-secret-a").fingerprint("AB1234");
        DocumentFingerprint second =
                new HmacDocumentFingerprintAdapter("test-secret-b").fingerprint("AB1234");

        assertThat(first.fingerprint()).isNotEqualTo(second.fingerprint());
        assertThat(first.last4()).isEqualTo(second.last4());
    }

    @Test
    void blankSecretIsRejectedAsConfigurationInvariant() {
        assertThatThrownBy(() -> new HmacDocumentFingerprintAdapter("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
