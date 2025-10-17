package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @CreationTimestamp
    private Instant createdAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}