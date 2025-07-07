package com.example.ibank.front.config;

import com.example.ibank.front.security.RestAuthManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http,
        RestAuthManager restAuthManager
    ) {
        var csrfHandler = new XorServerCsrfTokenRequestAttributeHandler();
        // should try to resolve the actual CSRF token from the body of multipart data requests
        csrfHandler.setTokenFromMultipartDataEnabled( true);
        return http
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers(
                    "/actuator/**",
                    "/favicon.ico",
                    "/login",
                    "/signup"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .csrf( csrf -> csrf.csrfTokenRequestHandler( csrfHandler))
            .formLogin( form -> form
                .loginPage( "/login")
                .authenticationManager( restAuthManager)
                .authenticationSuccessHandler( new RedirectServerAuthenticationSuccessHandler("/main"))
                .authenticationFailureHandler( ( exchange, exception) -> {
                    log.debug( "auth error: {}", exception.getMessage());
                    // стандартная переадресация при ошибке
                    var resp = exchange.getExchange().getResponse();
                    resp.getHeaders().setLocation( URI.create( "/login?error"));
                    resp.setStatusCode( HttpStatus.FOUND);
                    // передача детальной информации по ошибке для отображения на форме
                    return exchange.getExchange().getSession()
                        .doOnNext( ss -> {
                            ss.getAttributes().put( "errorInfo", exception.getMessage());
                        })
                        .then( Mono.empty());
                })
            )
            .logout( logout -> logout
                .logoutUrl( "/logout")
                .logoutSuccessHandler((exchange, authentication) ->
                    exchange.getExchange().getSession()
                        .flatMap( WebSession::invalidate) // удаляем сессию
                        .then( Mono.fromRunnable(() -> {
                            var resp = exchange.getExchange().getResponse();
                            // переход на страницу ввода логина/пароля после выхода
                            resp.setStatusCode( HttpStatus.FOUND);
                            resp.getHeaders().setLocation( URI.create( "/login"));
                        }))
                )
            )
            .build();
    }
}
