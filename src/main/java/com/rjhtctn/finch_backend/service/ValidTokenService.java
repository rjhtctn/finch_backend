package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.model.ValidToken;
import com.rjhtctn.finch_backend.repository.ValidTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidTokenService {

    private final ValidTokenRepository validTokenRepository;

    public ValidTokenService(ValidTokenRepository validTokenRepository) {
        this.validTokenRepository = validTokenRepository;
    }

    public void createTokenRecord(String jwtId, User user) {
        ValidToken validToken = new ValidToken(jwtId, user);
        validTokenRepository.save(validToken);
    }

    public boolean isTokenValidInDatabase(String jwtId) {
        return validTokenRepository.findByJwtId(jwtId).isPresent();
    }

    public void invalidateToken(String jwtId) {
        validTokenRepository.findByJwtId(jwtId).ifPresent(validTokenRepository::delete);
    }

    @Transactional
    public void invalidateAllTokensForUser(User user) {
        validTokenRepository.deleteAllByUserId(user.getId());
    }
}