package com.example.demo.service;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.User;
import com.example.demo.domain.request.UpdateFriendshipRequest;
import com.example.demo.domain.response.ResAceptFriendship;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.constant.FriendshipStatus;
import com.example.demo.util.errors.IdInvalidException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserService userService;
    public FriendshipService(FriendshipRepository friendshipRepository,@Lazy UserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }
    public int getTotalFriends(Long userId) {
        return friendshipRepository.countTotalFriends(userId);
    }
    public int getTotalPendingFriends(Long userId) {
        return friendshipRepository.countPendingFriendRequests(userId);
    }
    public List<ResAceptFriendship> getFriendship() throws IdInvalidException {
        List<ResAceptFriendship> resAceptFriendships = new ArrayList<>();
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        User currentUser = this.userService.handleFindUserByEmail(email);
        if (currentUser == null) {
            throw new IdInvalidException("User getFriendShip null");
        }
        List<Friendship> friendships = this.friendshipRepository.findByReceiverAndStatus(currentUser, FriendshipStatus.PENDING);
        for (Friendship friendship : friendships) {
            ResAceptFriendship dto = new ResAceptFriendship();
            dto.setId(friendship.getRequester().getId().intValue());
            dto.setName(friendship.getRequester().getUsername());
            dto.setAvatar(friendship.getRequester().getAvatarUrl());
            resAceptFriendships.add(dto);
        }
        return resAceptFriendships;
    }

    public Friendship handleUpdateFriendship(UpdateFriendshipRequest updateFriendshipRequest) throws IdInvalidException {
        if (updateFriendshipRequest == null) return null;

        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        User currentUser = this.userService.handleFindUserByEmail(email);

        if (currentUser == null) throw new IdInvalidException("user null");

        Friendship friendship = this.friendshipRepository
                .findByRequesterIdAndReceiverId(updateFriendshipRequest.getId(),currentUser.getId())
                .orElseThrow(() -> new IdInvalidException("friendship null"));

        // Ép kiểu status
        FriendshipStatus status;
        try {
            status = FriendshipStatus.valueOf(updateFriendshipRequest.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Trạng thái không hợp lệ");
        }

        friendship.setStatus(status);
        return this.friendshipRepository.save(friendship);
    }

}
