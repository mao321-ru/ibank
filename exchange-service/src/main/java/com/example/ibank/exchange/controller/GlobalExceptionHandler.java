package com.example.ibank.exchange.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Стандартный код для Access Denied (добавлен для исключения обработки универсальным обработчиком ниже)
    @ExceptionHandler( AuthorizationDeniedException.class)
    public Mono<ResponseEntity<String>> handleException(AuthorizationDeniedException e) {
        log.info( "HTTP 403: Access Denied");
        return Mono.just( ResponseEntity.status( HttpStatus.FORBIDDEN).body( "Access Denied"));
    }

    // Более информационное сообщение об ошибке
    @ExceptionHandler( RuntimeException.class)
    public Mono<ResponseEntity<String>> handleException(RuntimeException e) {
        String msg = e.getCause() != null
            ? "%s: %s".formatted( e.getMessage(), e.getCause().getMessage())
            : e.getMessage();
        log.error( "HTTP 500 response: %s".formatted( msg));
        log.error( "Error stack:", e);
        return Mono.just( ResponseEntity.internalServerError().body( msg));
    }

}
