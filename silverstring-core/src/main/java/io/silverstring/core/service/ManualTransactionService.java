package io.silverstring.core.service;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.exception.ExchangeException;
import io.silverstring.core.provider.MqPublisher;
import io.silverstring.core.repository.hibernate.ManualTransactionRepository;
import io.silverstring.core.repository.hibernate.TransactionRepository;
import io.silverstring.core.repository.hibernate.WalletRepository;
import io.silverstring.domain.dto.ManualTransactionDTO;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.StatusEnum;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.ManualTransaction;
import io.silverstring.domain.hibernate.Transaction;
import io.silverstring.domain.hibernate.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class ManualTransactionService {
    private final ManualTransactionRepository manualTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final WalletRepository walletRepository;
    private final MqPublisher mqPublisher;
    private final TransactionService transactionService;

    @Autowired
    public ManualTransactionService(ManualTransactionRepository manualTransactionRepository, TransactionRepository transactionRepository, ModelMapper modelMapper, WalletRepository walletRepository, MqPublisher mqPublisher, TransactionService transactionService) {
        this.manualTransactionRepository = manualTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.walletRepository = walletRepository;
        this.mqPublisher = mqPublisher;
        this.transactionService = transactionService;
    }

    public ManualTransactionDTO.ResManualTransaction getAll(CategoryEnum category, int pageNo, int pageSize) {
        ManualTransactionDTO.ResManualTransaction res = new ManualTransactionDTO.ResManualTransaction();
        res.setPageNo(pageNo);
        res.setPageSize(pageSize);

        Page<ManualTransaction> result = manualTransactionRepository.findAllByCategoryOrderByRegDtDesc(category, new PageRequest(pageNo, pageSize));
        if (result.getContent().size() <= 0) {
            res.setContents(new ArrayList<>());
            res.setPageTotalCnt(result.getTotalPages());
            return res;
        }

        res.setContents(result.getContent());
        res.setPageTotalCnt(result.getTotalPages());

        return res;
    }
}
