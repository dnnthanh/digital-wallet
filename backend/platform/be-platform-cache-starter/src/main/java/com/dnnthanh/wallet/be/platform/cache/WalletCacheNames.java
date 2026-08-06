package com.dnnthanh.wallet.be.platform.cache;

import lombok.experimental.UtilityClass;

/** Shared technical cache names; bounded contexts may contribute additional cache specs. */
@UtilityClass
public class WalletCacheNames {
    public final String AUTHORIZATION_SNAPSHOTS = "authorization-snapshots";
}
