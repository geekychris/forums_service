package com.example.forum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a forum. Forums can be nested within other forums.
 */
@Entity
@Table(name = "forums")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parentForum", "subForums", "posts", "forumAccesses"})
@EqualsAndHashCode(exclude = {"parentForum", "subForums", "posts", "forumAccesses"})
public class Forum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Self-referencing relationship for nested forums
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_forum_id")
    private Forum parentForum;

    @OneToMany(mappedBy = "parentForum", cascade = CascadeType.ALL)
    @Default
    private Set<Forum> subForums = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    @Default
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    @Default
    private Set<ForumAccess> forumAccesses = new HashSet<>();
}

