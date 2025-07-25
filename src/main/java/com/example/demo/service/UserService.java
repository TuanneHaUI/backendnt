package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.request.RegisterRequest;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
