package io.silverstring.web.controller.batch;

import io.silverstring.core.service.batch.DepositTransactionBatchService;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.web.controller.batch.thread.DepositThread;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/batch/depositTransaction")
public class DepositTransactionBatchController {
    final DepositTransactionBatchService depositTransactionBatchService;
    private final Environment environment;
    private final RedissonClient redissonClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public DepositTransactionBatchController(DepositTransactionBatchService depositTransactionBatchService, Environment environment, RedissonClient redissonClient) {
        this.depositTransactionBatchService = depositTransactionBatchService;
        this.environment = environment;
        this.redissonClient = redissonClient;
    }

    @Scheduled(fixedDelay=300_000, initialDelay=300_000)
    private synchronized void doPublishDepositTransaction() {
        String key = environment.getActiveProfiles()[0] + "_doPublishDepositTransaction";
        log.info("### doPublishDepositTransaction ready {} ### ", key);

        RLock lock = redissonClient.getLock(key);
        if (!lock.isLocked()) {
            lock.lock(5, TimeUnit.MINUTES);
            log.info("### doPublishDepositTransaction begin ###");

            for (CoinEnum coinEnum : CoinEnum.values()) {
                try {
                    DepositThread depositThread = applicationContext.getBean(DepositThread.class);
                    depositThread.setCoinName(coinEnum.name());
                    depositThread.start();
                } catch (Exception ex) {
                    log.error("coin : {} {}", coinEnum.name(), ex.getMessage());
                }
            }

            log.info("### doPublishDepositTransaction end ###");
            lock.unlock();
        }
    }
}
