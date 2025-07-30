package com.example.demo.controller;

import com.example.demo.domain.request.PostRequest;
import com.example.demo.domain.request.UpdateUserRequest;
import com.example.demo.domain.response.ResProfileDTO;
import com.example.demo.service.FileService;
import com.example.demo.util.errors.IdInvalidException;
import com.example.demo.util.errors.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.User;
import com.example.demo.domain.request.RegisterRequest;
import com.example.demo.domain.request.ReqLoginDTO;
import com.example.demo.domain.response.ResLoginDTO;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {
    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final FileService fileService;
    @Value("${tuanne.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;
    private final ObjectMapper objectMapper;
    @Value("${tuanne.upload-file.base-uri}")
    private String baseURI;
    public UserController(UserService userService, AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, FileService fileService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
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
        userLogin.setAvatar(currentUserDB.getAvatarUrl());
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

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token)
            throws IdInvalidException {
        System.out.println("==========refresh toke :"+refresh_token);
        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở Cookies");
        }

        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // check user by token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        System.out.println("==========refresh toke :"+currentUser);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ");
        }

        // issue new token/set refresh token as cookies
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB =this.userService.handleFindUserByEmail(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getUsername());
            res.setUser(userLogin);
        }
        String access_token = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);

        // update user
        this.userService.updateUserToken(new_refresh_token, email);

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResProfileDTO> getProfile() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        ResProfileDTO resProfileDTO = this.userService.hanleGetProfile(email);

        return ResponseEntity.ok(resProfileDTO);
}

    @PutMapping(value = "/updateUser", consumes = "multipart/form-data")
    public ResponseEntity<?> createPost(
            @RequestPart("user") String updateUser, // nhận chuỗi JSON
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder
    ) throws StorageException, URISyntaxException, IOException {
        System.out.println("Hẹ hẹ nè "+ updateUser);
        // Parse JSON từ chuỗi post
        UpdateUserRequest updateUserRequest = objectMapper.readValue(updateUser, UpdateUserRequest.class);
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.endsWith(item));

        if (!isValid) {
            throw new StorageException("Invalid file extension. only allows");
        }

        // create a directory if not exists
        this.fileService.createDirectory(baseURI + folder);
        // store file
        String uploadFile = this.fileService.store(file, folder);
        // Lấy user hiện tại
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (email == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if(email != null){
            return ResponseEntity.ok( this.userService.handleUpdateUser(email,updateUserRequest, uploadFile));
        }
        return ResponseEntity.ok("null");
    }


}
