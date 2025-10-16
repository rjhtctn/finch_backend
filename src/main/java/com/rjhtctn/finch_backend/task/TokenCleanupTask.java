package com.rjhtctn.finch_backend.task;

import com.rjhtctn.finch_backend.service.ValidTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class TokenCleanupTask {

    private final ValidTokenService validTokenService;

    public TokenCleanupTask(ValidTokenService validTokenService) {
        this.validTokenService = validTokenService;
    }

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Starting expired token cleanup task...");
        validTokenService.purgeExpiredTokens();
        log.info("Expired token cleanup task finished.");
    }
}