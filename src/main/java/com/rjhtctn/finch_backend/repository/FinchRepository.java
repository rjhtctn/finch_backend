package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Finch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FinchRepository extends JpaRepository<Finch, UUID> {}