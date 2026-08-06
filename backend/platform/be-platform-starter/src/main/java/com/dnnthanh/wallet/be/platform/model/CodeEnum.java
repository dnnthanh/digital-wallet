package com.dnnthanh.wallet.be.platform.model;

import com.fasterxml.jackson.annotation.JsonValue;

public interface CodeEnum {
    String name();

    @JsonValue
    default String getCode() {
        return name();
    }
}
