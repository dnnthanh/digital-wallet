package com.dnnthanh.wallet.be.platform.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.platform.i18n.I18nCodeEnum;
import org.junit.jupiter.api.Test;

class CodeEnumContractTest {
    @Test
    void codeContractDoesNotDependOnRuntimeEnumTypeChecks() {
        CodeEnum code = new SyntheticCode("CUSTOM_CODE");

        assertThat(code.getCode()).isEqualTo("CUSTOM_CODE");
    }

    @Test
    void i18nCodeContractBuildsMessageKeyWithoutRuntimeEnumTypeChecks() {
        I18nCodeEnum code = new SyntheticI18nCode("ACTIVE", AccountStatus.class);

        assertThat(code.getMessageKey()).isEqualTo("enum.AccountStatus.ACTIVE");
    }

    private record SyntheticCode(String name) implements CodeEnum {}

    private record SyntheticI18nCode(String name, Class<?> declaringClass) implements I18nCodeEnum {
        @Override
        public Class<?> getDeclaringClass() {
            return declaringClass;
        }
    }

    private enum AccountStatus {
        ACTIVE
    }
}
