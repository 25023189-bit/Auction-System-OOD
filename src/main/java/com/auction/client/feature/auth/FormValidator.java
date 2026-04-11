package com.auction.client.feature.auth;

public interface FormValidator<T> {
    ValidationResult validate(T form);
}