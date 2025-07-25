package com.example.demo.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private String author;
    private String avatar;
    private String content;
    private String image; // Có thể null nếu không có ảnh
    private int likes;
    private int comments;
}

