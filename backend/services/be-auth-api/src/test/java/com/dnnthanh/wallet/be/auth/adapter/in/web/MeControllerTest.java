package com.dnnthanh.wallet.be.auth.adapter.in.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dnnthanh.wallet.be.auth.application.model.MyProfile;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyPermissionsQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyProfileQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyScopesQuery;
import com.dnnthanh.wallet.be.platform.context.UserContext;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MeControllerTest {
    private GetMyProfileQuery profileQuery;
    private GetMyPermissionsQuery permissionsQuery;
    private GetMyScopesQuery scopesQuery;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        profileQuery = mock(GetMyProfileQuery.class);
        permissionsQuery = mock(GetMyPermissionsQuery.class);
        scopesQuery = mock(GetMyScopesQuery.class);
        mockMvc =
                MockMvcBuilders.standaloneSetup(
                                new MeController(profileQuery, permissionsQuery, scopesQuery))
                        .build();
    }

    @Test
    void shouldExposeCurrentProfileInPlatformEnvelope() throws Exception {
        when(profileQuery.getMyProfile())
                .thenReturn(
                        new MyProfile(
                                "user-1",
                                "wallet-demo",
                                UserContext.ActorType.USER,
                                Set.of("role:wallet-user")));

        mockMvc.perform(get("/private/api/v1/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.username").value("wallet-demo"))
                .andExpect(jsonPath("$.data.actorType").value("USER"));
    }

    @Test
    void shouldExposeEffectivePermissionsAndScopes() throws Exception {
        when(permissionsQuery.getMyPermissions()).thenReturn(Set.of("self:read"));
        when(scopesQuery.getMyScopes()).thenReturn(Set.of("/bank/demo-branch"));

        mockMvc.perform(get("/private/api/v1/me/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions[0]").value("self:read"));
        mockMvc.perform(get("/private/api/v1/me/scopes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scopes[0]").value("/bank/demo-branch"));
    }
}
