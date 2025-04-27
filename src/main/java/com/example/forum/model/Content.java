package com.example.forum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a piece of content (image, video, etc.) attached to a post or comment.
 */
@Entity
@Table(name = "contents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"post", "comment"})
@EqualsAndHashCode(exclude = {"post", "comment"})
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    private String description;

    @Column(nullable = false)
    private String contentPath;

    // Indicates if content is stored in the database or on disk
    @Column(nullable = false)
    private boolean storedInDb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    // Optional binary data if stored in DB
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}

