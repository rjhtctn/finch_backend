package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
@NoArgsConstructor
@Entity
public class ValidToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String jwtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date expiresAt;

    public ValidToken(String jwtId, User user, Date createdAt, Date expiresAt) {
        this.jwtId = jwtId;
        this.user = user;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}