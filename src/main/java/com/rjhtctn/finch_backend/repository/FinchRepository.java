package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import java.util.List;

@Repository
public interface FinchRepository extends JpaRepository<Finch, UUID> {

    List<Finch> findByUser_Username(String username, Sort sort);

    Page<Finch> findByUserIn(List<User> users, Pageable pageable);
}