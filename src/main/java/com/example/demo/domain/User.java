package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    private String avatarUrl;
    private String refreshToken;
    // Các quan hệ
    @OneToMany(mappedBy = "sender")
    private List<Message> messages;

    @ManyToMany(mappedBy = "participants")
    private List<Conversation> conversations;
    @Column(length = 500)
    private String bio; // Mô tả ngắn

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> posts;
    // Getter, Setter
}