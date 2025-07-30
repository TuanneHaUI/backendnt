package com.example.demo.service;

import com.example.demo.domain.Follow;
import com.example.demo.repository.FollowRepository;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    private final FollowRepository followRepository;


    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }
}
