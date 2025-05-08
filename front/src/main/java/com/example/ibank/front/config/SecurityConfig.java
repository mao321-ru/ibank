package com.example.ibank.front.config;

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
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain( ServerHttpSecurity http) {
        var csrfHandler = new XorServerCsrfTokenRequestAttributeHandler();
        // should try to resolve the actual CSRF token from the body of multipart data requests
        csrfHandler.setTokenFromMultipartDataEnabled( true);
        return http
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers(
                    "/favicon.ico",
                    "/login",
                    "/signup"
                ).permitAll()
                .anyExchange().authenticated()
            )
            .csrf( csrf -> csrf.csrfTokenRequestHandler( csrfHandler))
            .formLogin( form -> form
                .loginPage( "/login")
            )
            .logout( logout -> logout
                .logoutUrl( "/logout")
                .logoutSuccessHandler((exchange, authentication) ->
                    exchange.getExchange().getSession()
                        .flatMap( WebSession::invalidate) // удаляем сессию
                        .then( Mono.fromRunnable(() -> {
                            var resp = exchange.getExchange().getResponse();
                            // переход на витрину после выхода
                            resp.setStatusCode( HttpStatus.FOUND);
                            resp.getHeaders().setLocation( URI.create( "/"));
                        }))
                )
            )
            .build();
    }
}
