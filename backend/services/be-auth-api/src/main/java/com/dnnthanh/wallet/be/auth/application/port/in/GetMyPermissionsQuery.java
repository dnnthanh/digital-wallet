package com.dnnthanh.wallet.be.auth.application.port.in;

import java.util.Set;

public interface GetMyPermissionsQuery {
    Set<String> getMyPermissions();
}
