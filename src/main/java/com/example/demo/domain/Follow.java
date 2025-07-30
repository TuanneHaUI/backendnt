package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đi follow
    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // Người được follow
    @ManyToOne(optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(nullable = false)
    private LocalDateTime followedAt;

    @PrePersist
    protected void onCreate() {
        this.followedAt = LocalDateTime.now();
    }
}
