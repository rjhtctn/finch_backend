package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.ValidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidTokenRepository extends JpaRepository<ValidToken, Long> {

    Optional<ValidToken> findByJwtId(String jwtId);

    void deleteAllByUserId(UUID userId);
}