package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    private String refreshToken;

    @Column(length = 500)
    private String bio;

    // User gửi nhiều tin nhắn
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages;

    // User tham gia nhiều cuộc hội thoại
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Conversation> conversations;

    // User đăng nhiều bài viết
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> posts;

    // Lời mời kết bạn được gửi đi
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Friendship> sentFriendRequests;

    // Lời mời kết bạn nhận được
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Friendship> receivedFriendRequests;
}
