package com.auction.client.feature.auth;

/**
 * Hợp đồng validate cho từng loại form trước khi command gọi service.
 */
public interface FormValidator<T> {
    ValidationResult validate(T form);
}
