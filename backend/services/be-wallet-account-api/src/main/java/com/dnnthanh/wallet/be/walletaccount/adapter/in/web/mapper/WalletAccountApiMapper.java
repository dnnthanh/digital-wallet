package com.dnnthanh.wallet.be.walletaccount.adapter.in.web.mapper;

import com.dnnthanh.wallet.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.wallet.be.walletaccount.api.request.OpenWalletRequest;
import com.dnnthanh.wallet.be.walletaccount.api.response.WalletAccountResponse;
import com.dnnthanh.wallet.be.walletaccount.application.model.OpenWalletCommand;
import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface WalletAccountApiMapper {
    OpenWalletCommand requestToCommand(OpenWalletRequest request);

    WalletAccountResponse modelToResponse(WalletAccountView model);
}
