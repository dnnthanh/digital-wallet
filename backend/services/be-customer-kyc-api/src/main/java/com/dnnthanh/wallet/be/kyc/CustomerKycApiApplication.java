package com.dnnthanh.wallet.be.kyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CustomerKycApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerKycApiApplication.class, args);
    }
}
