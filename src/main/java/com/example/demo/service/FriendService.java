package com.example.demo.service;

import com.example.demo.repository.FriendshipRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendService {
    private final FriendshipRepository friendshipRepository;

    public FriendService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }
    public int getTotalFriends(Long userId) {
        return friendshipRepository.countTotalFriends(userId);
    }
    public int getTotalPendingFriends(Long userId) {
        return friendshipRepository.countPendingFriendRequests(userId);
    }
}
