package com.rjhtctn.finch_backend.task;

import com.rjhtctn.finch_backend.service.ValidTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupTask {

    private final ValidTokenService validTokenService;

    public TokenCleanupTask(ValidTokenService validTokenService) {
        this.validTokenService = validTokenService;
    }

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        System.out.println("Running expired token cleanup task...");
        validTokenService.purgeExpiredTokens();
        System.out.println("Expired token cleanup task finished.");
    }
}