package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefinchRepository extends JpaRepository<ReFinch, Long> {
    boolean existsByUserAndFinch(User user, Finch finch);
    Optional<ReFinch> findByUserAndFinch(User user, Finch finch);
    long countByFinch(Finch finch);
    boolean existsByFinch(Finch finch);
    Optional<List<ReFinch>>  findByUser(User user);
}
