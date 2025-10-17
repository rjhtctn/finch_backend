package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "finch_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReFinch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finch_id", nullable = false)
    private Finch finch;

    @CreationTimestamp
    private Instant createdAt;
}