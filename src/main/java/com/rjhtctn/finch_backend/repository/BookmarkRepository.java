package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Bookmark;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndFinch(User user, Finch finch);
    List<Bookmark> findAllByUser(User user);
    long countByFinch(Finch finch);
}