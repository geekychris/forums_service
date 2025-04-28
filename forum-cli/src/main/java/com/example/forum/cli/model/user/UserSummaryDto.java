package com.example.forum.cli.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for user summary information.
 * Contains basic user details for display purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSummaryDto {
    private Long id;
    private String username;
    private String displayName;
    private String email;
}
