package com.example.ibank.front.security;

import com.example.ibank.front.accounts.api.AuthApi;
import com.example.ibank.front.accounts.model.AuthResponse;
import com.example.ibank.front.accounts.model.RegisterRequest;
import com.example.ibank.front.accounts.model.RegisterResponse;
import com.example.ibank.front.accounts.model.ValidateRequest;
import com.example.ibank.front.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthApi authApi;

    public Mono<AuthResponse> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}", login);
        return authApi.validate( new ValidateRequest()
                .login( login)
                .password( password)
            )
            .onErrorResume( e -> Mono.error( new BadCredentialsException( e.getMessage())));
    }

    public Mono<RegisterResponse> register(SignupDto sd) {
        log.debug( "register: login: {}", sd.getLogin());
        return authApi.register( new RegisterRequest()
                        .login( sd.getLogin())
                        .password( sd.getPassword())
                        .userName( sd.getName())
                        .birthDate( sd.getBirthdate())
                );
    }
}