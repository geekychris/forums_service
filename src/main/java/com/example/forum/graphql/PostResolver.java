package com.example.forum.graphql;

import com.example.forum.model.Forum;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

@Controller
public class PostResolver {
    private final PostService postService;
    private final UserRepository userRepository;

    public PostResolver(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        
        // Handle different principal types
        if (principal instanceof User) {
            return ((User) principal).getId();
        } else if (principal instanceof String) {
            // If principal is a username string, find the user by username
            String username = (String) principal;
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                    .getId();
        } else {
            throw new SecurityException("Unexpected authentication principal type: " + 
                    (principal != null ? principal.getClass().getName() : "null"));
        }
    }

    @QueryMapping
    public Page<Post> posts(@Argument Long forumId, @Argument int page, @Argument int size) {
        return postService.getPostsByForum(forumId, getCurrentUserId(), PageRequest.of(page, size));
    }

    @QueryMapping
    public Post post(@Argument Long id) {
        return postService.getPostById(id, getCurrentUserId());
    }

    @QueryMapping
    public Page<Post> userPosts(@Argument Long authorId, @Argument int page, @Argument int size) {
        return postService.getPostsByUser(authorId, getCurrentUserId(), PageRequest.of(page, size));
    }

    @MutationMapping
    public Post createPost(
            @Argument String title,
            @Argument String content,
            @Argument Long forumId) {
        return postService.createPost(title, content, forumId, getCurrentUserId());
    }

    @MutationMapping
    public Post updatePost(
            @Argument Long id,
            @Argument String title,
            @Argument String content) {
        return postService.updatePost(id, title, content, getCurrentUserId());
    }

    @MutationMapping
    public boolean deletePost(@Argument Long id) {
        postService.deletePost(id, getCurrentUserId());
        return true;
    }
    // Field resolvers
    
    @SchemaMapping(typeName = "Post", field = "author")
    public User author(Post post) {
        return post.getUser();
    }
    
    @SchemaMapping(typeName = "Post", field = "forum")
    public Forum forum(Post post) {
        return post.getForum();
    }
    
    @SchemaMapping(typeName = "PostPage", field = "hasNext")
    public boolean hasNext(Page<Post> postPage) {
        return postPage.hasNext();
    }
    
    @SchemaMapping(typeName = "PostPage", field = "hasPrevious")
    public boolean hasPrevious(Page<Post> postPage) {
        return postPage.hasPrevious();
    }
}
