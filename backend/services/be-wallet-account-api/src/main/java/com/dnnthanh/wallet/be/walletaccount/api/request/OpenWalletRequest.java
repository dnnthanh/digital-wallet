package com.dnnthanh.wallet.be.walletaccount.api.request;

import jakarta.validation.constraints.NotBlank;

public record OpenWalletRequest(@NotBlank String currency) {}
