package com.example.forum.cli.model.graphql;

import com.example.forum.cli.model.post.PostResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLPostPageResponse {
    private List<PostResponse> content;
    private int totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean hasNext;
    private boolean hasPrevious;
}

