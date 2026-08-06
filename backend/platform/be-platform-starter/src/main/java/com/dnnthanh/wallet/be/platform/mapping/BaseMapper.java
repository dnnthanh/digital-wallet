package com.dnnthanh.wallet.be.platform.mapping;

public interface BaseMapper<REQUEST, MODEL, ENTITY, RESPONSE>
        extends RequestModelMapper<REQUEST, MODEL>,
                ModelEntityMapper<MODEL, ENTITY>,
                ModelResponseMapper<MODEL, RESPONSE> {}
