package com.dnnthanh.wallet.be.auth.adapter.in.web;

import com.dnnthanh.wallet.be.platform.security.ClientJwkProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientJwksController {
    private final ClientJwkProvider clientJwkProvider;
    private final ObjectMapper objectMapper;

    @GetMapping(
            value = "/.well-known/wallet-client-jwks.json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() throws JsonProcessingException {
        return objectMapper.writeValueAsString(clientJwkProvider.jwkSet());
    }
}
