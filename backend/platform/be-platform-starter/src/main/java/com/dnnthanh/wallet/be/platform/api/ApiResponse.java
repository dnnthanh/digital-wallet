package com.dnnthanh.wallet.be.platform.api;

import java.util.List;
import org.springframework.data.domain.Page;

/** Generic API envelope shared by servlet backend APIs. */
public record ApiResponse<T>(T data, ApiMetadata metadata, ApiError error) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, ApiMetadata metadata) {
        return new ApiResponse<>(data, metadata, null);
    }

    public static <T> ApiResponse<List<T>> page(Page<T> page) {
        return success(page.getContent(), PageMetadata.from(page));
    }

    public static ApiResponse<Void> failure(ApiError error) {
        return new ApiResponse<>(null, null, error);
    }
}
