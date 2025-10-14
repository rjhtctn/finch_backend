package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.Like;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndFinch(User user, Finch finch);
    List<Like> findAllByFinch(Finch finch);
    List<Like> findAllByUser(User user);
    int countByFinch(Finch finch);
}