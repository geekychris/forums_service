package com.example.forum.graphql;

import com.example.forum.model.Comment;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.CommentService;
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

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CommentResolver {
    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentResolver(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
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

    // Query methods
    
    @QueryMapping
    public Comment comment(@Argument Long id) {
        return commentService.getCommentById(id, getCurrentUserId());
    }
    
    @QueryMapping
    public Page<Comment> postComments(
            @Argument Long postId, 
            @Argument int page, 
            @Argument int size) {
        return commentService.getCommentsByPost(postId, getCurrentUserId(), PageRequest.of(page, size));
    }
    
    @QueryMapping
    public Page<Comment> commentReplies(
            @Argument Long commentId, 
            @Argument int page, 
            @Argument int size) {
        return commentService.getRepliesByComment(commentId, getCurrentUserId(), PageRequest.of(page, size));
    }
    
    @QueryMapping
    public Page<Comment> userComments(
            @Argument Long authorId, 
            @Argument int page, 
            @Argument int size) {
        return commentService.getCommentsByUser(authorId, getCurrentUserId(), PageRequest.of(page, size));
    }
    
    // Mutation methods
    
    @MutationMapping
    public Comment createComment(
            @Argument Long postId,
            @Argument String content) {
        return commentService.createComment(postId, content, getCurrentUserId());
    }
    
    @MutationMapping
    public Comment createReply(
            @Argument Long parentCommentId,
            @Argument String content) {
        return commentService.createReply(parentCommentId, content, getCurrentUserId());
    }
    
    @MutationMapping
    public Comment updateComment(
            @Argument Long id,
            @Argument String content) {
        return commentService.updateComment(id, content, getCurrentUserId());
    }
    
    @MutationMapping
    public boolean deleteComment(@Argument Long id) {
        commentService.deleteComment(id, getCurrentUserId());
        return true;
    }
    
    @MutationMapping
    public Comment upvoteComment(@Argument Long id) {
        return commentService.upvoteComment(id, getCurrentUserId());
    }
    
    @MutationMapping
    public Comment downvoteComment(@Argument Long id) {
        return commentService.downvoteComment(id, getCurrentUserId());
    }
    
    // Field resolvers
    
    @SchemaMapping(typeName = "Comment", field = "author")
    public User author(Comment comment) {
        return comment.getUser();
    }
    
    @SchemaMapping(typeName = "Comment", field = "post")
    public Post post(Comment comment) {
        return comment.getPost();
    }
    
    @SchemaMapping(typeName = "Comment", field = "parentComment")
    public Comment parentComment(Comment comment) {
        return comment.getParentComment();
    }
    
    @SchemaMapping(typeName = "Comment", field = "replies")
    public List<Comment> replies(Comment comment) {
        return comment.getReplies()
                .stream()
                .collect(Collectors.toList());
    }
    
    @SchemaMapping(typeName = "CommentPage", field = "hasNext")
    public boolean hasNext(Page<Comment> commentPage) {
        return commentPage.hasNext();
    }
    
    @SchemaMapping(typeName = "CommentPage", field = "hasPrevious")
    public boolean hasPrevious(Page<Comment> commentPage) {
        return commentPage.hasPrevious();
    }
}

