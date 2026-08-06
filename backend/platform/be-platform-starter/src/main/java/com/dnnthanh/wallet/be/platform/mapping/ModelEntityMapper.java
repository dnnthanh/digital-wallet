package com.dnnthanh.wallet.be.platform.mapping;

import java.util.List;

public interface ModelEntityMapper<MODEL, ENTITY> extends MapperContract {
    ENTITY modelToEntity(MODEL model);

    MODEL entityToModel(ENTITY entity);

    List<ENTITY> modelsToEntities(List<MODEL> models);

    List<MODEL> entitiesToModels(List<ENTITY> entities);
}
