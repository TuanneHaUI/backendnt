package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.domain.request.PostRequest;
import com.example.demo.domain.response.ResUploadFileDTO;
import com.example.demo.service.FileService;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.errors.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Value("${tuanne.upload-file.base-uri}")
    private String baseURI;

    public PostController(PostService postService, UserService userService, FileService fileService, ObjectMapper objectMapper) {
        this.postService = postService;
        this.userService = userService;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/createPost", consumes = "multipart/form-data")
    public ResponseEntity<?> createPost(
            @RequestPart("post") String postJson, // nhận chuỗi JSON
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder
    ) throws StorageException, URISyntaxException, IOException {

        // Parse JSON từ chuỗi post
        PostRequest postDto = objectMapper.readValue(postJson, PostRequest.class);

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

        User author = userService.handleFindUserByEmail(email);
        if(author != null){
            return ResponseEntity.ok(this.postService.handleCreatePost(postDto,author,uploadFile));
        }
        return ResponseEntity.ok("null");
    }
    @GetMapping("/getPost")
    public ResponseEntity<?> getPost(){
        return ResponseEntity.ok(this.postService.handleGetPost());
    }
}
