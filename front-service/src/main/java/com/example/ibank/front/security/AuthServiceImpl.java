package com.example.ibank.front.security;

import com.example.ibank.front.accounts.api.UserApi;
import com.example.ibank.front.accounts.model.*;
import com.example.ibank.front.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserApi usersApi;

    public Mono<UserInfo> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}", login);
        return usersApi.validate( login, new ValidateRequest().password( password))
            .onErrorResume( e -> Mono.error( new BadCredentialsException( e.getMessage())));
    }

    public Mono<UserInfo> register(SignupDto sd) {
        log.debug( "register: login: {}", sd.getLogin());
        return usersApi.createUser( new UserCreate()
                        .login( sd.getLogin())
                        .password( sd.getPassword())
                        .userName( sd.getName())
                        .birthDate( sd.getBirthdate())
                );
    }

    @Override
    public Mono<Void> changePassword(String login, String password) {
        return usersApi.changePassword( login, new ChangePasswordRequest().password( password));
    }

    @Override
    public boolean isAdult(LocalDate birthDate) {
        return !birthDate.plusYears(18).isAfter( LocalDate.now());
    }
}