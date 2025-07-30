package com.example.demo.repository;

import com.example.demo.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.status = 'ACCEPTED' AND (f.requester.id = :userId OR f.receiver.id = :userId)")
    int countTotalFriends(@Param("userId") Long userId);
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.status = 'PENDING' AND f.receiver.id = :userId")
    int countPendingFriendRequests(@Param("userId") Long userId);
}
