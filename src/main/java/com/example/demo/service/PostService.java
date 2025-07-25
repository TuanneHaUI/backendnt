package com.example.demo.service;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.domain.request.PostRequest;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
    public Post handleCreatePost(PostRequest post, User user, String nameFile){
        if(post != null){
            Post currentPost = new Post();
            if(user != null){
                currentPost.setAuthor(user);
                currentPost.setContent(post.getContent());
                currentPost.setCreatedAt(LocalDateTime.now());
                currentPost.setCommentCount(post.getComments());
                currentPost.setLikeCount(post.getLikes());
                currentPost.setImageUrl(nameFile);
            }
           return this.postRepository.save(currentPost);
        }
        return null;
    }
    public List<Post> handleGetPost(){
        return this.postRepository.findAll();
    }
}
