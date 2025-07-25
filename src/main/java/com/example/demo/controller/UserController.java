package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.User;
import com.example.demo.domain.request.RegisterRequest;
import com.example.demo.domain.request.ReqLoginDTO;
import com.example.demo.domain.response.ResLoginDTO;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;

import jakarta.validation.Valid;

@RestController
public class UserController {
    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    @Value("${tuanne.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;
    public UserController(UserService userService, AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if(!request.getPassword().equals(request.getConfirmPassword())){
            return ResponseEntity.ok("Mật khẩu không khớp");
        }
        User findUser = this.userService.handleFindUserByEmail(request.getEmail());
        if(findUser != null){
            return ResponseEntity.ok("Tài khoản đã tồn tại");
        }
        ClassLoader cl = this.getClass().getClassLoader();
        System.out.println("ClassLoader: " + cl.getClass().getName());
        this.userService.handleCreateUser(request);
        return ResponseEntity.ok("Đăng ký thành công! Đang chuyển đến trang đăng nhập...");
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ReqLoginDTO loginDto) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword());
                
        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        // create a token
        // set thông tin người dùng đăng nhập vào context( có thể dùng sao này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = userService.handleFindUserByEmail(loginDto.getEmail());
           if (currentUserDB != null) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(currentUserDB.getId());
        userLogin.setEmail(currentUserDB.getEmail());
        userLogin.setName(currentUserDB.getUsername());
        res.setUser(userLogin);          
                }
                
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDto.getEmail(), res);
        // update user
        this.userService.updateUserToken(refresh_token, loginDto.getEmail());

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }


    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
             System.out.println("==================>>>>>>>>>>> đã chạy vô đây chơi header");
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                System.out.println("==================>>>>>>>>>>>"+ email);
                if (email.equals("")) {
                       
                }

                this.userService.updateUserToken(null, email);

                ResponseCookie deleteSpringCookie = ResponseCookie
                                .from("refresh_token", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                                .body(null);
        }
}
