package com.dnnthanh.wallet.be.platform.api;

public record CursorMetadata(long totalElements, int size, String nextCursor, boolean hasNext)
        implements ApiMetadata {}
