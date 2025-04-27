package com.example.forum.service;

import com.example.forum.model.Content;
import com.example.forum.model.ContentType;
import com.example.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for managing post-related operations.
 */
public interface PostService {

    /**
     * Create a new post in a forum.
     *
     * @param title    the post title
     * @param content  the post content
     * @param forumId  the ID of the forum where to create the post
     * @param userId   the ID of the user creating the post
     * @return the created post
     */
    Post createPost(String title, String content, Long forumId, Long userId);

    /**
     * Get a post by ID.
     *
     * @param id      the post ID
     * @param userId  the ID of the user viewing the post (for permission check)
     * @return the post
     */
    Post getPostById(Long id, Long userId);

    /**
     * Update a post.
     *
     * @param id       the post ID
     * @param title    the new title (null if not changing)
     * @param content  the new content (null if not changing)
     * @param userId   the ID of the user updating the post
     * @return the updated post
     */
    Post updatePost(Long id, String title, String content, Long userId);

    /**
     * Delete a post.
     *
     * @param id      the post ID
     * @param userId  the ID of the user deleting the post
     */
    void deletePost(Long id, Long userId);

    /**
     * Get all posts in a forum, with pagination.
     *
     * @param forumId  the forum ID
     * @param userId   the ID of the user viewing the posts
     * @param pageable the pagination information
     * @return a page of posts
     */
    Page<Post> getPostsByForum(Long forumId, Long userId, Pageable pageable);

    /**
     * Get all posts by a user, with pagination.
     *
     * @param authorId the author user ID
     * @param userId   the ID of the user viewing the posts
     * @param pageable the pagination information
     * @return a page of posts
     */
    Page<Post> getPostsByUser(Long authorId, Long userId, Pageable pageable);

    /**
     * Search for posts by title or content.
     *
     * @param searchTerm the search term
     * @param userId     the ID of the user performing the search
     * @param pageable   the pagination information
     * @return a page of posts matching the search
     */
    Page<Post> searchPosts(String searchTerm, Long userId, Pageable pageable);

    /**
     * Search for posts in a specific forum by title or content.
     *
     * @param forumId    the forum ID
     * @param searchTerm the search term
     * @param userId     the ID of the user performing the search
     * @param pageable   the pagination information
     * @return a page of posts matching the search
     */
    Page<Post> searchPostsInForum(Long forumId, String searchTerm, Long userId, Pageable pageable);

    /**
     * Add content to a post.
     *
     * @param postId      the post ID
     * @param file        the file to upload
     * @param description the content description
     * @param contentType the type of content
     * @param storeInDb   whether to store the file in the database
     * @param userId      the ID of the user adding the content
     * @return the added content
     */
    Content addPostContent(Long postId, MultipartFile file, String description, 
                          ContentType contentType, boolean storeInDb, Long userId);

    /**
     * Get all content attached to a post.
     *
     * @param postId the post ID
     * @param userId the ID of the user accessing the content
     * @return a list of content items
     */
    List<Content> getPostContent(Long postId, Long userId);

    /**
     * Delete content from a post.
     *
     * @param contentId the content ID
     * @param userId    the ID of the user deleting the content
     */
    void deletePostContent(Long contentId, Long userId);
}

