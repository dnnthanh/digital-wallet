package com.dnnthanh.wallet.be.auth.application.port.out;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import java.util.Optional;

public interface AuthorizationCachePort {
    Optional<EffectiveAuthorization> find(String userId);

    void put(String userId, EffectiveAuthorization authorization);

    void evict(String userId);
}
