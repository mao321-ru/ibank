package com.example.ibank.front.controller;

import com.example.ibank.front.accounts.model.Account;
import com.example.ibank.front.accounts.model.UserShort;
import com.example.ibank.front.dto.EditPasswordDto;
import com.example.ibank.front.dto.EditUserAccountsDto;
import com.example.ibank.front.service.UserService;
import com.example.ibank.front.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
                    for (var errType: List.of( "passwordErrors", "userAccountsErrors")) {
                        model.addAttribute( errType, ss.getAttributes().getOrDefault( errType, null));
                        ss.getAttributes().remove( errType);
                    }
                })
            .thenReturn( "main");
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

}
