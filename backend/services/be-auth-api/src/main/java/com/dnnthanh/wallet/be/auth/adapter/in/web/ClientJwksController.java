package com.dnnthanh.wallet.be.auth.adapter.in.web;

import com.dnnthanh.wallet.be.platform.api.RawResponse;
import com.dnnthanh.wallet.be.platform.security.ClientJwkProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientJwksController {
    private final ClientJwkProvider clientJwkProvider;

    @RawResponse
    @GetMapping(
            value = "/.well-known/wallet-client-jwks.json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return clientJwkProvider.jwkSet();
    }
}
