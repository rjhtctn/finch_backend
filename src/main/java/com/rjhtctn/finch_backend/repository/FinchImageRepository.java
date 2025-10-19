package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.FinchImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FinchImageRepository extends JpaRepository<FinchImage, Long> {
    void deleteByFinchId(UUID finchId);
}