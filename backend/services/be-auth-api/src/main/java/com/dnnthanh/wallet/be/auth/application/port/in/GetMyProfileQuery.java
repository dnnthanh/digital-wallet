package com.dnnthanh.wallet.be.auth.application.port.in;

import com.dnnthanh.wallet.be.auth.application.model.MyProfile;

public interface GetMyProfileQuery {
    MyProfile getMyProfile();
}
