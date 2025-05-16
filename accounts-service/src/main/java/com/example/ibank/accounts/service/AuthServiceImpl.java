package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.ValidateRequest;
import com.example.ibank.accounts.model.AuthResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public Mono<AuthResponse> validate( ValidateRequest request) {
        log.debug( "validate for: username: {}", request.getUsername());
        // заглушка для успешной проверки user/user123
        return Mono.just(
                new AuthResponse()
                    .userId( "user")
                    .roles( List.of())
            )
            .filter( u ->
                u.getUserId().equals( request.getUsername())
                && u.getUserId().concat( "123").equals( request.getPassword())
            )
            .switchIfEmpty( Mono.error( new IllegalArgumentException( "Invalid username or password")))
        ;
    }

}
