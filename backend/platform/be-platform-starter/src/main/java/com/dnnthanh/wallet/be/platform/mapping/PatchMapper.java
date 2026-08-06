package com.dnnthanh.wallet.be.platform.mapping;

import org.mapstruct.MappingTarget;

public interface PatchMapper<SOURCE, TARGET> extends MapperContract {
    void update(SOURCE source, @MappingTarget TARGET target);
}
