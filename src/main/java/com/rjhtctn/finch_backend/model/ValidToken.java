package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, unique = true, length = 255)
    private String jwtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ValidToken(String jwtId, User user) {
        this.jwtId = jwtId;
        this.user = user;
    }
}