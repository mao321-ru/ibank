package com.example.ibank.front.service;

import com.example.ibank.front.accounts.api.UserApi;
import com.example.ibank.front.accounts.model.*;
import com.example.ibank.front.dto.EditUserAccountsDto;
import com.example.ibank.front.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserApi usersApi;

    public Mono<UserInfo> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}", login);
        return usersApi.validate( login, new ValidateRequest().password( password))
            .doOnError( e -> log.trace( "authenticate error: ", e))
            .onErrorResume( e -> Mono.error( new BadCredentialsException( e.getMessage())));
    }

    public Mono<UserInfo> register(SignupDto sd) {
        log.debug( "register: login: {}", sd.getLogin());
        return usersApi.createUser( new UserCreate()
                        .login( sd.getLogin())
                        .password( sd.getPassword())
                        .name( sd.getName())
                        .birthDate( sd.getBirthdate())
                );
    }

    @Override
    public Mono<Void> deleteUser(String login) {
        return usersApi.deleteUser( login);
    }

    @Override
    public Mono<Void> changePassword(String login, String password) {
        return usersApi.changePassword( login, new ChangePasswordRequest().password( password));
    }

    @Override
    public Mono<List<UserShort>> getUsers() {
        return usersApi.listUsers().collectList();
    }

    @Override
    public Mono<UserAccounts> getUserAccounts( String login) {
        return usersApi.getUserAccounts( login);
    }


    @Override
    public Mono<Void> editUserAccounts(String login, EditUserAccountsDto dto) {
        return usersApi.updateUserAccounts(
            login,
            new UserUpdateRequest()
                .name( dto.getName())
                .birthDate( dto.getBirthDate())
                .currencies( dto.getAccountCurrencies() != null ? dto.getAccountCurrencies() : List.of())
        );
    }

    @Override
    public boolean isAdult(LocalDate birthDate) {
        return !birthDate.plusYears(18).isAfter( LocalDate.now());
    }
}