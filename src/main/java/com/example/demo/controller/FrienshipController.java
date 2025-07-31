package com.example.demo.controller;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.request.UpdateFriendshipRequest;
import com.example.demo.domain.response.ResAceptFriendship;
import com.example.demo.service.FriendshipService;
import com.example.demo.util.errors.IdInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FrienshipController {
    private final FriendshipService friendshipService;

    public FrienshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }
    @GetMapping("/getAllFriend")
    public ResponseEntity<List<ResAceptFriendship>> getAllFriendship() throws IdInvalidException {
        List<ResAceptFriendship> resAceptFriendships = this.friendshipService.getFriendship();
        return ResponseEntity.ok(resAceptFriendships);
    }
    @PutMapping("/updateFriendShip")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateFriendshipRequest updateFriendshipRequest) throws IdInvalidException {
        if(updateFriendshipRequest == null){
            throw new IdInvalidException("updateFriendshipRequest null");
        }
        Friendship friendship = this.friendshipService.handleUpdateFriendship(updateFriendshipRequest);
        return ResponseEntity.ok(friendship);
    }
}
