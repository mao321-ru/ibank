package com.example.ibank.front.controller;

import com.example.ibank.front.dto.EditPasswordDto;
import com.example.ibank.front.dto.SignupDto;
import com.example.ibank.front.security.AuthService;
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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class MainController {

    private final AuthService authService;

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
            .then( exchange.getSession())
                .doOnNext( ss -> {
                    model.addAttribute(
                        "passwordErrors",
                        ss.getAttributes().getOrDefault( "passwordErrors", null)
                    );
                    ss.getAttributes().remove( "passwordErrors");
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
                    return authService.changePassword( login, dto.getPassword());
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

}
