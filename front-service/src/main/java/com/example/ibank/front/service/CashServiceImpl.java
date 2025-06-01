package com.example.ibank.front.service;

import com.example.ibank.front.accounts.api.UserApi;
import com.example.ibank.front.accounts.model.*;
import com.example.ibank.front.cash.api.CashApi;
import com.example.ibank.front.cash.model.CashOperationRequest;
import com.example.ibank.front.controller.enums.CashAction;
import com.example.ibank.front.dto.CashOperationDto;
import com.example.ibank.front.dto.EditUserAccountsDto;
import com.example.ibank.front.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {

    private final CashApi cashApi;

    @Override
    public Mono<Void> cashOperation(String login, CashOperationDto dto) {
        var rq = new CashOperationRequest()
            .login( login)
            .currency( dto.getCurrency())
            .amount( dto.getAmount())
        ;
        return
            switch ( dto.getAction()) {
                case CashAction.PUT -> cashApi.deposit( rq);
                case CashAction.GET -> cashApi.withdraw( rq);
            }
        ;
    }
}