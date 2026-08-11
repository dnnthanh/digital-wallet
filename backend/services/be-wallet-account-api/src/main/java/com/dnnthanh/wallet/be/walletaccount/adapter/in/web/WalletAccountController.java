package com.dnnthanh.wallet.be.walletaccount.adapter.in.web;

import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import com.dnnthanh.wallet.be.walletaccount.adapter.in.web.mapper.WalletAccountApiMapper;
import com.dnnthanh.wallet.be.walletaccount.api.request.OpenWalletRequest;
import com.dnnthanh.wallet.be.walletaccount.api.response.WalletAccountResponse;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.GetMyWalletQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.ListMyWalletsQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.OpenWalletAccountUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private/api/v1/wallets")
@RequiredArgsConstructor
public class WalletAccountController {
    private final OpenWalletAccountUseCase openUseCase;
    private final ListMyWalletsQuery listQuery;
    private final GetMyWalletQuery getQuery;
    private final WalletAccountApiMapper mapper;

    @PostMapping
    @PreAuthorize("@walletAuthorization.hasPermission('WALLET_SELF_CREATE')")
    public ApiResponse<WalletAccountResponse> openWallet(@Valid @RequestBody OpenWalletRequest request) {
        return ApiResponse.success(mapper.modelToResponse(openUseCase.open(mapper.requestToCommand(request))));
    }

    @GetMapping("/me")
    @PreAuthorize("@walletAuthorization.hasPermission('WALLET_SELF_READ')")
    public ApiResponse<List<WalletAccountResponse>> listMyWallets() {
        return ApiResponse.success(listQuery.listMyWallets().stream().map(mapper::modelToResponse).toList());
    }

    @GetMapping("/{walletId}")
    @PreAuthorize("@walletAuthorization.hasPermission('WALLET_SELF_READ')")
    public ApiResponse<WalletAccountResponse> getMyWallet(@PathVariable UUID walletId) {
        return ApiResponse.success(mapper.modelToResponse(getQuery.getMyWallet(walletId)));
    }
}
