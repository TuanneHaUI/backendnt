package com.example.demo.service;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.domain.request.RegisterRequest;
import com.example.demo.domain.response.ResProfileDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.errors.IdInvalidException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PostService postService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postService = postService;
    }
    public User handleFindUserByEmail(String email) {
        return userRepository.findByEmail(email); // Có thể trả về null
    }
    public void handleCreateUser(RegisterRequest request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        String hashPassword = this.passwordEncoder.encode(request.getPassword());
        user.setPassword(hashPassword);
        this.userRepository.save(user);
    }
    public void updateUserToken(String token, String email) {
        User currentUser = this.handleFindUserByEmail(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }
    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
    public ResProfileDTO hanleGetProfile(String email) throws IdInvalidException {
        User currentUser = handleFindUserByEmail(email);
        ResProfileDTO resProfileDTO = new ResProfileDTO();
        resProfileDTO.setName(currentUser.getUsername());
        resProfileDTO.setBio(currentUser.getBio());
        resProfileDTO.setAvatar(currentUser.getAvatarUrl());
        List<Post> currentPost = this.postService.handleGetPostByIdUser(currentUser);
        if(currentPost == null){
            throw new IdInvalidException("Post null");
        }
        List<String> imagesPost = new ArrayList<>();
        AtomicLong totalPost = new AtomicLong();
        currentPost.forEach(post -> {
            if (post.getImageUrl() != null) {
                imagesPost.add(post.getImageUrl());
                totalPost.addAndGet(1);
            }
        });
        resProfileDTO.setUrlImages(imagesPost);
        resProfileDTO.setPost(totalPost.intValue());
        return resProfileDTO;
    }
}
