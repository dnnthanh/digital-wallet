package com.dnnthanh.wallet.be.auth.adapter.in.web;

import com.dnnthanh.wallet.be.auth.api.response.MyPermissionsResponse;
import com.dnnthanh.wallet.be.auth.api.response.MyScopesResponse;
import com.dnnthanh.wallet.be.auth.application.model.MyProfile;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyPermissionsQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyProfileQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyScopesQuery;
import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private/api/v1/me")
@RequiredArgsConstructor
public class MeController {
    private final GetMyProfileQuery profileQuery;
    private final GetMyPermissionsQuery permissionsQuery;
    private final GetMyScopesQuery scopesQuery;

    @GetMapping("/profile")
    public ApiResponse<MyProfile> profile() {
        return ApiResponse.success(profileQuery.getMyProfile());
    }

    @GetMapping("/permissions")
    public ApiResponse<MyPermissionsResponse> permissions() {
        return ApiResponse.success(new MyPermissionsResponse(permissionsQuery.getMyPermissions()));
    }

    @GetMapping("/scopes")
    public ApiResponse<MyScopesResponse> scopes() {
        return ApiResponse.success(new MyScopesResponse(scopesQuery.getMyScopes()));
    }
}
