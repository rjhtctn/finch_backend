package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"user", "likes"})
@ToString(exclude = {"user", "likes"})
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Finch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 280)
    private String content;

    @OneToMany(mappedBy = "finch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinchImage> images =  new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "finch", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Finch parentFinch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quoted_finch_id")
    private Finch quotedFinch;

    @OneToMany(mappedBy = "parentFinch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Finch> replies = new ArrayList<>();

    @OneToMany(mappedBy = "finch", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReFinch> reFinches = new HashSet<>();
}