package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.context.SystemUserContextFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/** Technical actor contexts available to Kafka consumers, schedulers and services. */
@AutoConfiguration
@Import(SystemUserContextFactory.class)
public class PlatformExecutionContextAutoConfiguration {}
