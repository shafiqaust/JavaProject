package io.silverstring.web.listener.mq;

import io.silverstring.core.service.TransactionService;
import io.silverstring.core.service.batch.WithdrawalTransactionBatchService;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WithdrawalTransactionMqListener {
    private final WithdrawalTransactionBatchService withdrawalTransactionBatchService;
    private final TransactionService transactionService;

    @Autowired
    public WithdrawalTransactionMqListener(WithdrawalTransactionBatchService withdrawalTransactionBatchService,  TransactionService transactionService) {
        this.withdrawalTransactionBatchService = withdrawalTransactionBatchService;
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = "withdrawal_transactions")
    public void onMessage(final WalletDTO.TransactionInfo transactionInfo) {
        try {
            log.info("* withdrawal_transactions::onMessage : {}", transactionInfo);
            withdrawalTransactionBatchService.doTransaction(transactionInfo);
        } catch(Exception ex) {
            log.error("* withdrawal_transactions onMessage ERROR : {}", ex.getMessage());
            log.error(ex.getMessage(), ex);
        }
    }
}
