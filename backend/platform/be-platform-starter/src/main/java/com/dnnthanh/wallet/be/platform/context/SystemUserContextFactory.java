package com.dnnthanh.wallet.be.platform.context;

public class SystemUserContextFactory {
    public UserContext forKafkaConsumer(String serviceName) {
        return UserContext.system(serviceName, UserContext.ActorType.KAFKA_CONSUMER);
    }

    public UserContext forScheduler(String serviceName) {
        return UserContext.system(serviceName, UserContext.ActorType.SCHEDULER);
    }

    public UserContext forService(String serviceName) {
        return UserContext.system(serviceName, UserContext.ActorType.SERVICE);
    }
}
