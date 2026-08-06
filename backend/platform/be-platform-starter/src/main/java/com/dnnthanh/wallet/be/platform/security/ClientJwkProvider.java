package com.dnnthanh.wallet.be.platform.security;

import java.util.Map;

public interface ClientJwkProvider {
    Map<String, Object> jwkSet();
}
