package com.rjhtctn.finch_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class FinchImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column
    private String fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finch_id")
    private Finch finch;
}