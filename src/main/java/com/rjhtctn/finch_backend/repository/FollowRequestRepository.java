package com.rjhtctn.finch_backend.repository;

import com.rjhtctn.finch_backend.model.FollowRequest;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    Optional<FollowRequest> findBySenderAndReceiver(User sender, User receiver);
    List<FollowRequest> findByReceiverAndStatus(User receiver, FollowRequest.Status status);
}