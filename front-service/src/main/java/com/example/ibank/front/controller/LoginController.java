package com.example.ibank.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class LoginController {

    @GetMapping( "/login")
    Mono<String> login(
            ServerWebExchange exchange,
            Model model
    ) {
        boolean isError = exchange.getRequest().getQueryParams().containsKey("error");
        log.debug( "login: isError: {}", isError);
        return exchange.getSession()
            .doOnNext( ss -> {
                boolean isErrorInfo = ss.getAttributes().containsKey( "errorInfo");
                model.addAttribute(
                    "errorInfo",
                    isErrorInfo ? ss.getAttributes().get( "errorInfo") : null
                );
                if( isErrorInfo) ss.getAttributes().remove( "errorInfo");
            })
            .thenReturn( "login");
    }

    @GetMapping( "/signup")
    Mono<String> signup(
        ServerWebExchange exchange,
        Model model
    ) {
        log.debug( "signup:");
        return Mono.just( "signup");
    }

    @GetMapping( "/logout")
    Mono<String> logout(
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug( "logout:");
        return Mono.just( "logout");
    }

}
