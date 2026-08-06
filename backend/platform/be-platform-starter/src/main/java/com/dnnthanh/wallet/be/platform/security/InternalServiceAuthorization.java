package com.dnnthanh.wallet.be.platform.security;

import com.dnnthanh.wallet.be.platform.context.UserContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

@Component("internalServiceAuthorization")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InternalServiceAuthorization {
    private final UserContext userContext;

    public InternalServiceAuthorization(UserContext userContext) {
        this.userContext = userContext;
    }

    public boolean isServiceAccount() {
        return userContext.roles().contains("SERVICE_ACCOUNT");
    }
}
