package com.example.ibank.front.security;

import com.example.ibank.front.accounts.api.AuthApi;
import com.example.ibank.front.accounts.model.AuthResponse;
import com.example.ibank.front.accounts.model.ValidateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthApi authApi;

    public Mono<AuthResponse> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}", login);
        return authApi.validate( new ValidateRequest()
                .login( login)
                .password( password)
            )
            .onErrorResume( e -> Mono.error( new BadCredentialsException( e.getMessage())));
    }
}