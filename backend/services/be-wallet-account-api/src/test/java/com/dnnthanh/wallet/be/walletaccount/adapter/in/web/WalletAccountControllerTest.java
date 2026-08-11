package com.dnnthanh.wallet.be.walletaccount.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dnnthanh.wallet.be.walletaccount.adapter.in.web.mapper.WalletAccountApiMapper;
import com.dnnthanh.wallet.be.walletaccount.api.request.OpenWalletRequest;
import com.dnnthanh.wallet.be.walletaccount.application.model.WalletAccountView;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.GetMyWalletQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.ListMyWalletsQuery;
import com.dnnthanh.wallet.be.walletaccount.application.port.in.OpenWalletAccountUseCase;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletStatus;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WalletAccountControllerTest {
    @Mock private OpenWalletAccountUseCase openUseCase;
    @Mock private ListMyWalletsQuery listQuery;
    @Mock private GetMyWalletQuery getQuery;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WalletAccountController controller =
                new WalletAccountController(
                        openUseCase,
                        listQuery,
                        getQuery,
                        Mappers.getMapper(WalletAccountApiMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void openWalletMapsRequestAndResponseWithoutBalanceField() throws Exception {
        when(openUseCase.open(any())).thenReturn(view());

        mockMvc.perform(
                        post("/private/api/v1/wallets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currency\":\"VND\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.data.currency").value("VND"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.balance").doesNotExist());
    }

    @Test
    void endpointsDeclareRequiredEffectivePermissions() throws Exception {
        assertPermission("openWallet", "WALLET_SELF_CREATE", OpenWalletRequest.class);
        assertPermission("listMyWallets", "WALLET_SELF_READ");
        assertPermission("getMyWallet", "WALLET_SELF_READ", UUID.class);
    }

    private static void assertPermission(
            String methodName, String permission, Class<?>... parameterTypes) throws Exception {
        Method method = WalletAccountController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).contains(permission);
    }

    private static WalletAccountView view() {
        Instant now = Instant.parse("2026-08-11T08:00:00Z");
        return new WalletAccountView(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "user-1",
                "/bank/demo-branch",
                "VND",
                WalletStatus.ACTIVE,
                now,
                now);
    }
}
