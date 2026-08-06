package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.platform.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class UserContextCurrentActorAdapter implements CurrentActorPort {
    private final UserContext userContext;

    @Override
    public String userId() {
        return userContext.userId();
    }
}
