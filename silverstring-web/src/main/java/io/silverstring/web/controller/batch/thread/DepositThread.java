package io.silverstring.web.controller.batch.thread;

import io.silverstring.core.service.batch.DepositTransactionBatchService;
import io.silverstring.domain.enums.CoinEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class DepositThread extends Thread {
    @Autowired
    private ApplicationContext applicationContext;
    private String coinName;

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    @Override
    public void run() {
        try {
            log.debug(">>>>>>>> " + coinName + " >>>>>> DepositThread >>>>>>>>>> start " + super.getId());
            DepositTransactionBatchService depositTransactionBatchService = applicationContext.getBean(DepositTransactionBatchService.class);
            depositTransactionBatchService.doPublishTransaction(CoinEnum.valueOf(coinName));
            log.debug(">>>>>>>> " + coinName + " >>>>>> DepositThread >>>>>>>>>> stop " + super.getId());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
