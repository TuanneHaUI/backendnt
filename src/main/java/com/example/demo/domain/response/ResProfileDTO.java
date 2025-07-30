package com.example.demo.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResProfileDTO {
    private String name;
    private String bio;
    private String avatar;
    private int post;
    private int follower;
    private int following;
    private List<String> urlImages;
}
