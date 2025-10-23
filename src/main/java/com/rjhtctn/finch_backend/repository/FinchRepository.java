package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinchRepository extends JpaRepository<Finch, UUID> {

    List<Finch> findByUser_Username(String username, Sort sort);

    List<Finch> findAllByParentFinchIsNullOrderByCreatedAtDesc();

    List<Finch> findByUserInAndParentFinchIsNull(List<User> users, Sort sort);

    Page<Finch> findByContentContainingIgnoreCase(String query, Pageable pageable);
}