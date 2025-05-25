package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.*;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> validate( ValidateRequest request);

    Mono<RegisterResponse> register(RegisterRequest request);

    Mono<Boolean> changePassword(ChangePasswordRequest request);

}
