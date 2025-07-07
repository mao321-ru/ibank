package com.example.ibank.front.security;

import com.example.ibank.front.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthManager implements ReactiveAuthenticationManager {

    private final UserService userService;
    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();
        return userService.authenticate(login, password)
                .switchIfEmpty( Mono.error( new BadCredentialsException("Invalid credentials")))
                .map(resp -> new UsernamePasswordAuthenticationToken(
                        AuthUser.builder()
                                .login( resp.getLogin())
                                .userName( resp.getName())
                                .birthDate( resp.getBirthDate())
                                .build(),
                        null,
                        List.of()
                ))
                .doFinally( signalType ->
                    Counter.builder( "ibank_authenticate")
                        .tag( "login", login)
                        .tag( "result", signalType == SignalType.ON_COMPLETE ? "OK" : "ERROR")
                        .register( meterRegistry)
                        .increment()
                )
                .cast( Authentication.class)
        ;
    }
}