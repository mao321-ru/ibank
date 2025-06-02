package com.example.ibank.transfer.controller;

import com.example.ibank.transfer.api.TransferApi;
import com.example.ibank.transfer.model.*;

import com.example.ibank.transfer.service.TransferService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize( "hasRole('TRANSFER_API')")
public class TransferController implements TransferApi {

    private final TransferService srv;

    public Mono<ResponseEntity<Void>> transfer(
        Mono<TransferRequest> transferRequest,
        final ServerWebExchange exchange
    )
    {
        return transferRequest
            .flatMap( req -> srv.transfer( req))
            .thenReturn( ResponseEntity.noContent().build())
        ;
    }

}
