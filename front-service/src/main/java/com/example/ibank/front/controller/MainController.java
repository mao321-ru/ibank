package com.example.ibank.front.controller;

import com.example.ibank.front.accounts.model.Account;
import com.example.ibank.front.accounts.model.UserShort;
import com.example.ibank.front.controller.enums.ErrorSource;
import com.example.ibank.front.dto.CashOperationDto;
import com.example.ibank.front.dto.EditPasswordDto;
import com.example.ibank.front.dto.EditUserAccountsDto;
import com.example.ibank.front.dto.TransferDto;
import com.example.ibank.front.service.MoneyService;
import com.example.ibank.front.service.UserService;
import com.example.ibank.front.security.AuthUser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class MainController {

    private final UserService userService;
    private final MoneyService moneyService;

    private final MeterRegistry meterRegistry;

    @GetMapping("/")
    public Mono<String> redirectRoot() {
        return Mono.just("redirect:/main");
    }

    @GetMapping( "/main")
    Mono<String> mainPage(
        ServerWebExchange exchange,
        Model model
    ) {
        log.debug( "main");
        return exchange.getPrincipal()
                .cast( UsernamePasswordAuthenticationToken.class)
                .map( UsernamePasswordAuthenticationToken::getPrincipal)
                .cast( AuthUser.class)
                .doOnNext( user -> {
                    log.debug("user: {}", user);
                    model.addAttribute("login", user.getLogin());
                    model.addAttribute("name", user.getUserName());
                    model.addAttribute(
                        "birthdate",
                        user.getBirthDate().format( DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    );
                })
            .flatMap( user ->
                Mono.when(
                    userService.getUsers()
                        .doOnNext( users ->
                            model.addAttribute( "users",
                                users.stream()
                                    // исключаем себя из отправки "Другому пользователю"
                                    .filter( u -> ! u.getLogin().equals( user.getLogin()))
                                    .sorted( Comparator.comparing( UserShort::getName))
                            )
                        )
                        .onErrorComplete( e -> {
                            log.debug( "getUsers error: {}", e.getMessage());
                            model.addAttribute("getUsersErrors", List.of( e.getMessage()));
                            return true;
                        }),
                    userService.getUserAccounts( user.getLogin())
                        .doOnNext( ua -> {
                            model.addAttribute("name", ua.getName());
                            model.addAttribute(
                                "birthdate",
                                ua.getBirthDate().format( DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            );
                            model.addAttribute("accounts", ua.getAccounts());
                            model.addAttribute(
                                "currency",
                                ua.getAccounts().stream().map( Account::getCurrency).toList()
                            );
                        })
                        .onErrorComplete( e -> {
                            log.debug( "getUserAccounts error: {}", e.getMessage());
                            model.addAttribute("getAccountsErrors", List.of( e.getMessage()));
                            return true;
                        })
                )
            )
            .then( exchange.getSession())
                .doOnNext( ss -> {
                    for (var src: ErrorSource.values()) {
                        String paramName = src.getParamName();
                        var errors = ss.getAttributes().getOrDefault( paramName, null);
                        if ( errors != null) log.debug( "show errors: {}: {}", paramName, errors);
                        model.addAttribute( paramName, errors);
                        ss.getAttributes().remove( paramName);
                    }
                })
            .thenReturn( "main");
    }

    @PostMapping( "/user/{login}/deleteUser")
    Mono<ResponseEntity<Void>> deleteUser(
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug( "deleteUser");
        return exchange.getPrincipal()
            .map( Principal::getName)
            .flatMap( login -> {
                log.debug("login: {}", login);
                return userService.deleteUser( login);
            })
            .then( exchange.getSession())
            .flatMap( session -> {
                // очистка сессии после удаления пользователя и переход на страницу логина
                session.invalidate();
                return Mono.just( ResponseEntity
                    .status( HttpStatus.FOUND)
                    .header("Location", "/login")
                    .header("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"")
                    .<Void>build());
            })
            .onErrorResume( e -> exchange.getSession()
                    .doOnNext( ss -> {
                        log.debug( "set deleteUserErrors: {}", e.getMessage());
                        ss.getAttributes().put( "deleteUserErrors", List.of( e.getMessage()));
                    })
                    .thenReturn(
                        ResponseEntity.status( HttpStatus.FOUND)
                            .location( URI.create("/main"))
                            .build()
                    )
            );
    }

    @PostMapping( "/user/{login}/editPassword")
    Mono<String> editPassword(
            EditPasswordDto dto,
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug( "editPassword");
        return exchange.getPrincipal()
                .map( Principal::getName)
                .flatMap( login -> {
                    log.debug("login: {}", login);
                    String errorMessage =
                        StringUtils.isEmpty( dto.getPassword()) ? "Не заполнено [Пароль]" :
                        ! dto.getPassword().equals( dto.getConfirmPassword()) ?  "Указаны различные пароли" :
                        null
                    ;
                    if( errorMessage != null) throw new IllegalArgumentException( errorMessage);
                    return userService.changePassword( login, dto.getPassword());
                })
                .onErrorResume( e -> exchange.getSession()
                    .doOnNext( ss -> {
                        log.debug( "set passwordErrors: {}", e.getMessage());
                        ss.getAttributes().put( "passwordErrors", List.of( e.getMessage()));
                    })
                    .then( Mono.empty())
                )
                .thenReturn( "redirect:/main");
    }

    @PostMapping( "/user/{login}/editUserAccounts")
    Mono<String> editUserAccounts(
            EditUserAccountsDto dto,
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug( "editUserAccounts");
        return exchange.getPrincipal()
                .map( Principal::getName)
                .flatMap( login -> {
                    log.debug("login: {}", login);
                    log.debug("dto: {}", dto);
                    if( dto.getBirthDate() != null && ! userService.isAdult( dto.getBirthDate())) {
                        throw new IllegalArgumentException( "Возраст дожен быть не менее 18 лет");
                    }
                    return userService.editUserAccounts( login, dto);
                })
                .onErrorResume( e -> exchange.getSession()
                        .doOnNext( ss -> {
                            log.debug( "userAccountsErrors: {}", e.getMessage());
                            ss.getAttributes().put( "userAccountsErrors", List.of( e.getMessage()));
                        })
                        .then( Mono.empty())
                )
                .thenReturn( "redirect:/main");
    }

    @PostMapping( "/user/{login}/cash")
    Mono<String> cashOperation(
            CashOperationDto dto,
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug( "cashOperation");
        return exchange.getPrincipal()
                .map( Principal::getName)
                .flatMap( login -> {
                    log.debug("login: {}", login);
                    log.debug("dto: {}", dto);
                    return moneyService.cashOperation( login, dto);
                })
                .onErrorResume( e -> exchange.getSession()
                        .doOnNext( ss -> {
                            String paramName = ErrorSource.CashAction.getParamName();
                            log.debug( "save errors: {}: {}", paramName, e.getMessage());
                            ss.getAttributes().put( paramName, List.of( e.getMessage()));
                        })
                        .then( Mono.empty())
                )
                .thenReturn( "redirect:/main");
    }

    @PostMapping( "/user/{login}/transfer")
    Mono<String> transfer(
            TransferDto dto,
            ServerWebExchange exchange,
            Model model
    ) {
        return exchange.getPrincipal()
                .map( Principal::getName)
                .flatMap( login -> {
                    log.debug("transfer: login: {}, dto: {}", login, dto);
                    boolean isToOther =  ! login.equals( dto.getToLogin());
                    return Mono.just( login)
                        .flatMap( v -> {
                            String errorMessage =
                                ! isToOther && dto.getFromCurrency().equals( dto.getToCurrency())
                                    ? "Для перевода между своими счетами нужно указать различные валюты"
                                : StringUtils.isEmpty( dto.getToLogin())
                                    ? "Нужно указать получателя платежа"
                                : null
                            ;
                            if (errorMessage != null) throw new IllegalArgumentException( errorMessage);
                            return moneyService.transfer( login, dto)
                                .doOnError( e ->
                                    Counter.builder( "ibank_transfer_error")
                                        .tag( "login", login)
                                        .tag( "currency", dto.getFromCurrency())
                                        .tag( "to_login", dto.getToLogin())
                                        .tag( "to_currency", dto.getToCurrency())
                                        .register( meterRegistry)
                                        .increment()
                                )
                            ;
                        })
                        .onErrorResume( e -> exchange.getSession()
                            .doOnNext( ss -> {
                                String paramName = ! isToOther
                                    ? ErrorSource.Transfer.getParamName()
                                    : ErrorSource.TransferOther.getParamName()
                                ;
                                log.debug( "save errors: {}: {}", paramName, e.getMessage());
                                ss.getAttributes().put( paramName, List.of( e.getMessage()));
                            })
                            .then( Mono.empty())
                        );
                })
                .thenReturn( "redirect:/main");
    }

}
