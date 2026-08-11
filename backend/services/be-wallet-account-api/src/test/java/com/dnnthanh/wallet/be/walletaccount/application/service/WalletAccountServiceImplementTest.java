package com.dnnthanh.wallet.be.walletaccount.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.application.model.OpenWalletCommand;
import com.dnnthanh.wallet.be.walletaccount.application.model.VerifiedKyc;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.KycVerificationPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import com.dnnthanh.wallet.be.walletaccount.infrastructure.WalletAccountProperties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletAccountServiceImplementTest {
    private static final Instant NOW = Instant.parse("2026-08-11T07:30:00Z");

    @Mock private CurrentActorPort currentActorPort;
    @Mock private KycVerificationPort kycVerificationPort;
    @Mock private WalletAccountWriteTransaction writeTransaction;
    @Mock private WalletAccountRepositoryPort repository;

    private WalletAccountServiceImplement service;

    @BeforeEach
    void setUp() {
        WalletCurrencyPolicy currencyPolicy =
                new WalletCurrencyPolicy(
                        new WalletAccountProperties(
                                URI.create("http://localhost:8082"), Set.of("VND", "USD")));
        service =
                new WalletAccountServiceImplement(
                        currentActorPort,
                        kycVerificationPort,
                        writeTransaction,
                        repository,
                        currencyPolicy);
    }

    @Test
    void verifiesKycBeforeInvokingTransactionalWriter() {
        WalletAccount wallet = wallet("user-1", "VND", NOW);
        when(currentActorPort.userId()).thenReturn("user-1");
        when(kycVerificationPort.requireVerified("user-1"))
                .thenReturn(new VerifiedKyc("user-1", "/bank/demo-branch"));
        when(writeTransaction.open("user-1", "/bank/demo-branch", "VND")).thenReturn(wallet);

        var result = service.open(new OpenWalletCommand(" vnd "));

        InOrder order = inOrder(kycVerificationPort, writeTransaction);
        order.verify(kycVerificationPort).requireVerified("user-1");
        order.verify(writeTransaction).open("user-1", "/bank/demo-branch", "VND");
        assertThat(result.walletId()).isEqualTo(wallet.walletId());
        assertThat(result.currency()).isEqualTo("VND");
    }

    @Test
    void kycFailurePreventsDatabaseWriterInvocation() {
        when(currentActorPort.userId()).thenReturn("user-1");
        when(kycVerificationPort.requireVerified("user-1"))
                .thenThrow(new BusinessException(WalletAccountErrorCode.WALLET_KYC_REQUIRED));

        assertThatThrownBy(() -> service.open(new OpenWalletCommand("VND")))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(WalletAccountErrorCode.WALLET_KYC_REQUIRED));
        verify(writeTransaction, never()).open("user-1", "/bank/demo-branch", "VND");
    }

    @Test
    void getUsesOwnerScopedRepositoryLookup() {
        UUID walletId = UUID.randomUUID();
        WalletAccount wallet = wallet("user-1", "VND", NOW);
        when(currentActorPort.userId()).thenReturn("user-1");
        when(repository.findByIdAndUserId(walletId, "user-1")).thenReturn(Optional.of(wallet));

        var result = service.getMyWallet(walletId);

        assertThat(result.userId()).isEqualTo("user-1");
        verify(repository).findByIdAndUserId(walletId, "user-1");
    }

    @Test
    void missingOwnedWalletReturnsStableNotFound() {
        UUID walletId = UUID.randomUUID();
        when(currentActorPort.userId()).thenReturn("user-1");
        when(repository.findByIdAndUserId(walletId, "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyWallet(walletId))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(WalletAccountErrorCode.WALLET_NOT_FOUND));
    }

    @Test
    void listIsDeterministicByCreationTimeThenWalletId() {
        UUID laterId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        UUID earlierId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        WalletAccount later =
                WalletAccount.create(
                        laterId, "user-1", "/bank/demo-branch", "USD", NOW.plusSeconds(10));
        WalletAccount earlier =
                WalletAccount.create(
                        earlierId, "user-1", "/bank/demo-branch", "VND", NOW);
        when(currentActorPort.userId()).thenReturn("user-1");
        when(repository.findAllByUserId("user-1")).thenReturn(List.of(later, earlier));

        var result = service.listMyWallets();

        assertThat(result).extracting(view -> view.walletId()).containsExactly(earlierId, laterId);
    }

    private static WalletAccount wallet(String userId, String currency, Instant createdAt) {
        return WalletAccount.create(
                UUID.randomUUID(), userId, "/bank/demo-branch", currency, createdAt);
    }
}
