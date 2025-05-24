package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.RegisterRequest;
import com.example.ibank.accounts.model.RegisterResponse;
import com.example.ibank.accounts.model.ValidateRequest;
import com.example.ibank.accounts.model.AuthResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> validate( ValidateRequest request);

    Mono<RegisterResponse> register(RegisterRequest request);

}
