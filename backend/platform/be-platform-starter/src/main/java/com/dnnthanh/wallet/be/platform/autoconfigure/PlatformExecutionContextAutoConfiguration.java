package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.context.SystemUserContextFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Technical actor contexts available to Kafka consumers, schedulers and services. */
@Configuration(proxyBeanMethods = false)
@Import(SystemUserContextFactory.class)
public class PlatformExecutionContextAutoConfiguration {}
