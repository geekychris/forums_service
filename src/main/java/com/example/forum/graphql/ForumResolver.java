package com.example.forum.graphql;

import com.example.forum.model.Forum;
import com.example.forum.service.ForumService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import com.example.forum.model.User;
import com.example.forum.repository.UserRepository;

import java.util.List;

@Controller
public class ForumResolver {
    private final ForumService forumService;
    private final UserRepository userRepository;

    public ForumResolver(ForumService forumService, UserRepository userRepository) {
        this.forumService = forumService;
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
    public List<Forum> forums() {
        return forumService.getRootForums();
    }

    @QueryMapping
    public Forum forum(@Argument Long id) {
        return forumService.getForumById(id);
    }

    @MutationMapping
    public Forum createForum(
            @Argument String name,
            @Argument String description) {
        return forumService.createForum(name, description, getCurrentUserId());
    }

    @MutationMapping
    public Forum updateForum(
            @Argument Long id,
            @Argument String name,
            @Argument String description) {
        return forumService.updateForum(id, name, description, getCurrentUserId());
    }

    @MutationMapping
    public boolean deleteForum(@Argument Long id) {
        forumService.deleteForum(id, getCurrentUserId());
        return true;
    }
}

