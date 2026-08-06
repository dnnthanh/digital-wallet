package com.dnnthanh.wallet.be.auth.application.port.out;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;

public interface AuthorizationDirectoryPort {
    EffectiveAuthorization resolve(String userId);
}
