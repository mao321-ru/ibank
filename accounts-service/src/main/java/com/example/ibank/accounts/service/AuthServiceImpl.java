package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.ValidateRequest;
import com.example.ibank.accounts.model.AuthResponse;
import com.example.ibank.accounts.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<AuthResponse> validate( ValidateRequest request) {
        log.debug( "validate for: login: {}", request.getLogin());
        return repo.findByLogin( request.getLogin())
            .doOnNext( u -> log.trace( "found userId: {}", u.getId()))
            .filter( u -> passwordEncoder.matches( request.getPassword(), u.getPasswordHash()))
            .map( u -> new AuthResponse()
                .login( u.getLogin())
                .roles( List.of())
            )
            .switchIfEmpty( Mono.error( new IllegalArgumentException( "Invalid username or password")))
        ;
    }

}
