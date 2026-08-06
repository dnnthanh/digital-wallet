package com.dnnthanh.wallet.be.platform.mapping;

import java.util.List;

public interface RequestModelMapper<REQUEST, MODEL> extends MapperContract {
    MODEL requestToModel(REQUEST request);

    List<MODEL> requestsToModels(List<REQUEST> requests);
}
