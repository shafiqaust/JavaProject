package io.silverstring.web.controller.rest;

import io.silverstring.core.service.TransactionService;
import io.silverstring.core.util.KeyGenUtil;
import io.silverstring.domain.dto.Response;
import io.silverstring.domain.dto.TransactionDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.StatusEnum;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.ManualTransaction;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/transaction")
public class TransactionRestController {
    final TransactionService transactionService;
    final ModelMapper modelMapper;

    @Autowired
    public TransactionRestController(TransactionService transactionService, ModelMapper modelMapper) {
        this.transactionService = transactionService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/requestWithdrawal")
    public Response<TransactionDTO.ResWithdrawal> requestWithdrawal(@ModelAttribute User user, @RequestBody TransactionDTO.ReqWithdrawal request) {
        ManualTransaction manualTransaction = new ManualTransaction();
        manualTransaction.setId(KeyGenUtil.generateTxId());
        manualTransaction.setUserId(user.getId());
        manualTransaction.setCoin(new Coin(CoinEnum.valueOf(request.getCoinName())));
        manualTransaction.setCategory(CategoryEnum.send);
        manualTransaction.setAddress(request.getAddress());
        manualTransaction.setAmount(request.getAmount());
        manualTransaction.setStatus(StatusEnum.PENDING);
        manualTransaction.setRegDt(LocalDateTime.now());

        return Response.<TransactionDTO.ResWithdrawal>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(modelMapper.map(transactionService.regist(user, manualTransaction, request), TransactionDTO.ResWithdrawal.class))
                .build();
    }

    @PostMapping("/cancelWithdrawal")
    public Response<TransactionDTO.ResCancelWithdrawal> cancelWithdrawal(@ModelAttribute User user, @RequestBody TransactionDTO.ReqCancelWithdrawal request) {
        return Response.<TransactionDTO.ResCancelWithdrawal>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(transactionService.cancel(user, request))
                .build();
    }

    @PostMapping("/getTransactions")
    public Response<TransactionDTO.ResTransactions> getTransactions(@ModelAttribute User user, @RequestBody TransactionDTO.ReqTransactions request) {
        return Response.<TransactionDTO.ResTransactions>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(modelMapper.map(transactionService.getTransactions(user.getId(), request), TransactionDTO.ResTransactions.class))
                .build();
    }
}
