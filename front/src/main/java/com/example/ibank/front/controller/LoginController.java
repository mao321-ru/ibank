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

    @GetMapping( "/signup")
    Mono<String> signup(
        ServerWebExchange exchange,
        Model model
    ) {
        log.debug( "signup:");
        return Mono.just( "signup");
    }

}
