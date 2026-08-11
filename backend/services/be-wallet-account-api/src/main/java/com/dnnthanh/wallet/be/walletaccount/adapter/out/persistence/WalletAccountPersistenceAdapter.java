package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.stereotype.Persistence;
import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.repository.WalletAccountJpaRepository;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

@Persistence
@RequiredArgsConstructor
public class WalletAccountPersistenceAdapter implements WalletAccountRepositoryPort {
    private static final String USER_CURRENCY_CONSTRAINT = "uk_wallet_account_user_currency";

    private final WalletAccountJpaRepository repository;
    private final WalletAccountPersistenceMapper mapper;

    @Override
    public Optional<WalletAccount> findByIdAndUserId(UUID walletId, String userId) {
        return repository.findByWalletIdAndUserId(walletId, userId).map(mapper::entityToModel);
    }

    @Override
    public List<WalletAccount> findAllByUserId(String userId) {
        return repository.findAllByUserIdOrderByCreatedAtAscWalletIdAsc(userId).stream()
                .map(mapper::entityToModel)
                .toList();
    }

    @Override
    public WalletAccount save(WalletAccount wallet) {
        try {
            return mapper.entityToModel(repository.saveAndFlush(mapper.modelToEntity(wallet)));
        } catch (DataIntegrityViolationException exception) {
            if (hasConstraint(exception, USER_CURRENCY_CONSTRAINT)) {
                throw new BusinessException(WalletAccountErrorCode.WALLET_ALREADY_EXISTS);
            }
            throw exception;
        }
    }

    private static boolean hasConstraint(Throwable throwable, String constraintName) {
        return ExceptionUtils.getThrowableList(throwable).stream()
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast)
                .map(ConstraintViolationException::getConstraintName)
                .anyMatch(constraintName::equals);
    }
}
