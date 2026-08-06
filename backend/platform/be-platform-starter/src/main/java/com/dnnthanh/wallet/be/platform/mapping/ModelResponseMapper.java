package com.dnnthanh.wallet.be.platform.mapping;

import java.util.List;

public interface ModelResponseMapper<MODEL, RESPONSE> extends MapperContract {
    RESPONSE modelToResponse(MODEL model);

    List<RESPONSE> modelsToResponses(List<MODEL> models);
}
