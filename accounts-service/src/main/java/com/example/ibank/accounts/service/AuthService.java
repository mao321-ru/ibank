package com.example.ibank.accounts.service;

import com.example.ibank.accounts.domain.ValidateRequest;
import com.example.ibank.accounts.domain.AuthResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> validate( ValidateRequest request);

}
