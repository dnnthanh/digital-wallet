package com.dnnthanh.wallet.be.walletaccount.application.service;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.application.model.OpenWalletCommand;
import com.dnnthanh.wallet.be.walletaccount.application.model.VerifiedKyc;
import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.GetMyWalletQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.ListMyWalletsQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.OpenWalletAccountUseCase;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.KycVerificationPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletAccountServiceImplement
        implements OpenWalletAccountUseCase, GetMyWalletQuery, ListMyWalletsQuery {
    private final CurrentActorPort currentActorPort;
    private final KycVerificationPort kycVerificationPort;
    private final WalletAccountWriteTransaction writeTransaction;
    private final WalletAccountRepositoryPort repository;
    private final WalletCurrencyPolicy currencyPolicy;

    @Override
    public WalletAccountView open(OpenWalletCommand command) {
        String userId = currentActorPort.userId();
        String currency = currencyPolicy.normalizeSupported(command.currency());
        VerifiedKyc verifiedKyc = kycVerificationPort.requireVerified(userId);
        return WalletAccountView.from(
                writeTransaction.open(userId, verifiedKyc.scopePath(), currency));
    }

    @Override
    public WalletAccountView getMyWallet(UUID walletId) {
        String userId = currentActorPort.userId();
        return repository
                .findByIdAndUserId(walletId, userId)
                .map(WalletAccountView::from)
                .orElseThrow(() -> new BusinessException(WalletAccountErrorCode.WALLET_NOT_FOUND));
    }

    @Override
    public List<WalletAccountView> listMyWallets() {
        String userId = currentActorPort.userId();
        return repository.findAllByUserId(userId).stream()
                .map(WalletAccountView::from)
                .sorted(
                        Comparator.comparing(WalletAccountView::createdAt)
                                .thenComparing(WalletAccountView::walletId))
                .toList();
    }
}
