package com.auction.client.auth;

public interface FormValidator<T> {
    ValidationResult validate(T form);
}