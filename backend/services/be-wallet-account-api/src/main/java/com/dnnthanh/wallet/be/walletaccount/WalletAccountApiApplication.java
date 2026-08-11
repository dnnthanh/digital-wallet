package com.dnnthanh.wallet.be.walletaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {
            "com.dnnthanh.wallet.be.walletaccount",
            "com.dnnthanh.wallet.be.platform.autoconfigure"
        })
@ConfigurationPropertiesScan
public class WalletAccountApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletAccountApiApplication.class, args);
    }
}
