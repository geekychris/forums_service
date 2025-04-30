package com.forum.app;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Factory class for creating common dialog UI components.
 * This class provides methods for creating and displaying dialogs
 * with consistent styling and behavior throughout the application.
 */
public class DialogFactory {
    
    private static final int PADDING = 20;
    private static final int TEXT_FIELD_WIDTH = 30;
    private static final int TEXT_AREA_ROWS = 10;
    private static final int TEXT_AREA_COLS = 40;
    
    /**
     * Private constructor to prevent instantiation.
     * All methods are static.
     */
    private DialogFactory() {
        // Utility class, no instances
    }
    
    /**
     * Show an error dialog.
     * 
     * @param parent Parent component
     * @param title Dialog title
     * @param message Error message
     */
    public static void showErrorDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show an information dialog.
     * 
     * @param parent Parent component
     * @param title Dialog title
     * @param message Information message
     */
    public static void showInfoDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show a confirmation dialog.
     * 
     * @param parent Parent component
     * @param title Dialog title
     * @param message Confirmation message
     * @return true if confirmed, false otherwise
     */
    public static boolean showConfirmDialog(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show dialog to create a new forum.
     * 
     * @param parent Parent frame
     * @param parentForumId Parent forum ID (null for top-level forums)
     */
    public static void showCreateForumDialog(Frame parent, Long parentForumId) {
        JDialog dialog = new JDialog(parent, "Create New Forum", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Forum name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(TEXT_FIELD_WIDTH);
        panel.add(nameField, gbc);
        
        // Forum description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea descriptionArea = new JTextArea(TEXT_AREA_ROWS / 2, TEXT_AREA_COLS);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton createButton = new JButton("Create");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            // Validate input
            if (name.isEmpty() || name.length() < 3) {
                showErrorDialog(dialog, "Validation Error", "Forum name must be at least 3 characters long.");
                return;
            }
            
            // Create forum request
            ApiClient.createForum(name, description, parentForumId, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Forum created successfully.");
                    // Refresh forum tree here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to create forum: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        panel.add(buttonPanel, gbc);
        
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to edit an existing forum.
     * 
     * @param parent Parent frame
     * @param forum Forum to edit
     */
    public static void showEditForumDialog(Frame parent, ForumDto forum) {
        if (forum == null) {
            showErrorDialog(parent, "Error", "No forum selected for editing.");
            return;
        }
        
        JDialog dialog = new JDialog(parent, "Edit Forum", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Forum name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(TEXT_FIELD_WIDTH);
        nameField.setText(forum.getName());
        panel.add(nameField, gbc);
        
        // Forum description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea descriptionArea = new JTextArea(TEXT_AREA_ROWS / 2, TEXT_AREA_COLS);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setText(forum.getDescription());
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            // Validate input
            if (name.isEmpty() || name.length() < 3) {
                showErrorDialog(dialog, "Validation Error", "Forum name must be at least 3 characters long.");
                return;
            }
            
            // Update forum request
            ApiClient.updateForum(forum.getId(), name, description, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Forum updated successfully.");
                    // Refresh forum tree here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to update forum: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        panel.add(buttonPanel, gbc);
        
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to create a new post.
     * 
     * @param parent Parent frame
     * @param forumId Forum ID for the new post
     */
    public static void showCreatePostDialog(Frame parent, Long forumId) {
        if (forumId == null) {
            showErrorDialog(parent, "Error", "No forum selected for the new post.");
            return;
        }
        
        JDialog dialog = new JDialog(parent, "Create New Post", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Post title
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(TEXT_FIELD_WIDTH);
        panel.add(titleField, gbc);
        
        // Post content
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Content:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea contentArea = new JTextArea(TEXT_AREA_ROWS, TEXT_AREA_COLS);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton createButton = new JButton("Create");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        createButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            
            // Validate input
            if (title.isEmpty() || title.length() < 3) {
                showErrorDialog(dialog, "Validation Error", "Post title must be at least 3 characters long.");
                return;
            }
            
            if (content.isEmpty() || content.length() < 10) {
                showErrorDialog(dialog, "Validation Error", "Post content must be at least 10 characters long.");
                return;
            }
            
            // Create post request
            ApiClient.createPost(title, content, forumId, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Post created successfully.");
                    // Refresh post list here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to create post: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        panel.add(buttonPanel, gbc);
        
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(true);
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to edit an existing post.
     * 
     * @param parent Parent frame
     * @param post Post to edit
     */
    public static void showEditPostDialog(Frame parent, PostDto post) {
        if (post == null) {
            showErrorDialog(parent, "Error", "No post selected for editing.");
            return;
        }
        
        JDialog dialog = new JDialog(parent, "Edit Post", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Post title
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(TEXT_FIELD_WIDTH);
        titleField.setText(post.getTitle());
        panel.add(titleField, gbc);
        
        // Post content
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Content:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea contentArea = new JTextArea(TEXT_AREA_ROWS, TEXT_AREA_COLS);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setText(post.getContent());
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            
            // Validate input
            if (title.isEmpty() || title.length() < 3) {
                showErrorDialog(dialog, "Validation Error", "Post title must be at least 3 characters long.");
                return;
            }
            
            if (content.isEmpty() || content.length() < 10) {
                showErrorDialog(dialog, "Validation Error", "Post content must be at least 10 characters long.");
                return;
            }
            
            // Update post request
            ApiClient.updatePost(post.getId(), title, content, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Post updated successfully.");
                    // Refresh post list here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to update post: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        panel.add(buttonPanel, gbc);
        
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(true);
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to create a new comment on a post.
     * 
     * @param parent Parent frame
     * @param postId Post ID for the new comment
     * @param parentCommentId Parent comment ID (null for top-level comments)
     */
    public static void showCreateCommentDialog(Frame parent, Long postId, Long parentCommentId) {
        if (postId == null && parentCommentId == null) {
            showErrorDialog(parent, "Error", "No post or parent comment selected.");
            return;
        }
        
        String title = parentCommentId == null ? "Add Comment" : "Reply to Comment";
        JDialog dialog = new JDialog(parent, title, true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Comment content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Comment:"), BorderLayout.NORTH);
        
        JTextArea contentArea = new JTextArea(TEXT_AREA_ROWS / 2, TEXT_AREA_COLS);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton submitButton = new JButton("Submit");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        submitButton.addActionListener(e -> {
            String content = contentArea.getText().trim();
            
            // Validate input
            if (content.isEmpty() || content.length() < 1) {
                showErrorDialog(dialog, "Validation Error", "Comment cannot be empty.");
                return;
            }
            
            // Create comment request
            ApiClient.createComment(content, postId != null ? postId : 0L, parentCommentId, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Comment submitted successfully.");
                    // Refresh comment list here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to submit comment: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(panel);
        dialog.getRootPane().setDefaultButton(submitButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to edit an existing comment.
     * 
     * @param parent Parent frame
     * @param comment Comment to edit
     */
    public static void showEditCommentDialog(Frame parent, CommentDto comment) {
        if (comment == null) {
            showErrorDialog(parent, "Error", "No comment selected for editing.");
            return;
        }
        
        JDialog dialog = new JDialog(parent, "Edit Comment", true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Comment content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Comment:"), BorderLayout.NORTH);
        
        JTextArea contentArea = new JTextArea(TEXT_AREA_ROWS / 2, TEXT_AREA_COLS);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setText(comment.getContent());
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
            String content = contentArea.getText().trim();
            
            // Validate input
            if (content.isEmpty() || content.length() < 1) {
                showErrorDialog(dialog, "Validation Error", "Comment cannot be empty.");
                return;
            }
            
            // Update comment request
            ApiClient.updateComment(comment.getId(), content, response -> {
                if (response.isSuccess()) {
                    dialog.dispose();
                    showInfoDialog(parent, "Success", "Comment updated successfully.");
                    // Refresh comment list here if needed
                } else {
                    showErrorDialog(dialog, "Error", "Failed to update comment: " + response.getErrorMessage());
                }
                return null;
            });
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(panel);
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
}
