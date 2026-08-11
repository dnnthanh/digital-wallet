package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence;

import com.dnnthanh.wallet.be.platform.mapping.ModelEntityMapper;
import com.dnnthanh.wallet.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.entity.WalletAccountEntity;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface WalletAccountPersistenceMapper
        extends ModelEntityMapper<WalletAccount, WalletAccountEntity> {}
