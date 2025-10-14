package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"user", "finch"})
@ToString(exclude = {"user", "finch"})
@NoArgsConstructor
@Entity
@Table(name = "finch_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "finch_id"})
})
public class Like {

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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Like(User user, Finch finch) {
        this.user = user;
        this.finch = finch;
    }
}