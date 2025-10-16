package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.model.ValidToken;
import com.rjhtctn.finch_backend.repository.ValidTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
public class ValidTokenService {

    private final ValidTokenRepository validTokenRepository;

    public ValidTokenService(ValidTokenRepository validTokenRepository) {
        this.validTokenRepository = validTokenRepository;
    }

    @Transactional
    public void createTokenRecord(String jwtId, User user, Date createdAt, Date expiresAt) {
        ValidToken token = new ValidToken(jwtId, user, createdAt, expiresAt);
        validTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public boolean isTokenValidInDatabase(String jwtId) {
        return validTokenRepository.findByJwtId(jwtId).isPresent();
    }

    @Transactional
    public void invalidateToken(String jwtId) {
        validTokenRepository.findByJwtId(jwtId).ifPresent(validTokenRepository::delete);
    }

    @Transactional
    public void invalidateAllTokensForUser(User user) {
        validTokenRepository.deleteAllByUser(user);
    }

    @Transactional
    public void purgeExpiredTokens() {
        int before = (int) validTokenRepository.count();
        validTokenRepository.deleteAllByExpiresAtBefore(new Date());
        int after = (int) validTokenRepository.count();
    }
}