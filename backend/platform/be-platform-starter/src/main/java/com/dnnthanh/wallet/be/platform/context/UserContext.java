package com.dnnthanh.wallet.be.platform.context;

import com.dnnthanh.wallet.be.platform.model.CodeEnum;
import java.util.Set;

public record UserContext(String userId, String username, ActorType actorType, Set<String> roles) {
    public enum ActorType implements CodeEnum {
        USER,
        SERVICE,
        SCHEDULER,
        KAFKA_CONSUMER
    }

    public static UserContext system(String serviceName, ActorType actorType) {
        return new UserContext(serviceName, serviceName, actorType, Set.of("SERVICE_ACCOUNT"));
    }
}
