package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.Follow;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    List<Follow> findAllByFollower(User follower);

    List<Follow> findAllByFollowing(User following);
}