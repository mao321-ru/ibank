package com.example.ibank.front.controller;

import com.example.ibank.front.dto.SignupDto;
import com.example.ibank.front.security.AuthService;
import com.example.ibank.front.security.RestAuthManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class LoginController {

    private final AuthService srv;
    private final RestAuthManager restAuthManager;

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
    Mono<String> signupGet(
        ServerWebExchange exchange,
        Model model
    ) {
        log.debug( "signupGet");
        return Mono.just( "signup");
    }

    @PostMapping( "/signup")
    Mono<String> signupPost(
        SignupDto sd,
        ServerWebExchange exchange,
        Model model
    ) {
        log.debug( "signupPost: login: {}", sd.getLogin());
        String errorMessage =
            StringUtils.isEmpty( sd.getLogin()) ? "Не заполнено [Логин]" :
            StringUtils.isEmpty( sd.getPassword()) ? "Не заполнено [Пароль]" :
            StringUtils.isEmpty( sd.getName()) ? "Не заполнено [Фамилия Имя]" :
            sd.getBirthdate() == null ? "Не заполнено [Дата рождения]" :
            ! sd.getPassword().equals( sd.getConfirmPassword()) ?  "Указаны различные пароли" :
            null
        ;
        return ( errorMessage != null
                ? Mono.error( new IllegalArgumentException( errorMessage))
                : srv.register( sd)
            )
            .switchIfEmpty( Mono.error( new IllegalStateException( "Response not found")))
            .then(
                restAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken( sd.getLogin(), sd.getPassword())
                )
                .flatMap( auth -> {
                    SecurityContextImpl sc = new SecurityContextImpl();
                    sc.setAuthentication( auth);
                    return exchange.getSession()
                        .doOnNext( webSession ->
                            webSession.getAttributes().put( SPRING_SECURITY_CONTEXT_KEY, sc)
                        )
                        .then();
                })
            )
            .thenReturn( "redirect:/main")
            .onErrorResume( e -> {
                log.debug( "signupPost: errorMessage: {}", e.getMessage());
                model.addAttribute("login", sd.getLogin());
                model.addAttribute(
                    "password",
                    Objects.equals( sd.getPassword(), sd.getConfirmPassword()) ? sd.getPassword() : ""
                );
                model.addAttribute("name", sd.getName());
                model.addAttribute("birthdate", sd.getBirthdate());
                model.addAttribute("errors", List.of( e.getMessage()));
                return Mono.just( "signup");
            });
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
