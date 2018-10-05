package io.silverstring.core.provider;

import io.silverstring.domain.dto.MessagePacket;
import io.silverstring.domain.dto.UserSearchDTO;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.hibernate.EmailConfirm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqPublisher {
    final RabbitTemplate rabbitTemplate;

    @Autowired
    public MqPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void depositTransactionInfoPublish(WalletDTO.TransactionInfo transactionInfo) {
        rabbitTemplate.convertAndSend("exchange", "deposit_transactions", transactionInfo);
    }

    public void withdrawalTransactionInfoPublish(WalletDTO.TransactionInfo transactionInfo) {
        rabbitTemplate.convertAndSend("exchange", "withdrawal_transactions", transactionInfo);
    }
}
