package com.example.ibank.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class MainController {

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
            .map( Principal::getName)
            .doOnNext( login -> {
                log.debug("login: {}", login);
                model.addAttribute("login", login);
            })
            .thenReturn( "main");
    }

}
