package com.forum.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.json.JSONObject;

/**
 * Dialog for user authentication.
 */
public class LoginPanel extends JDialog {
    
    private final MainFrame parentFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JButton registerButton;
    
    // Authentication token storage
    private static String jwtToken = null;
    private static Long userId = null;
    private static String username = null;
    private static String userRole = null;
    private static String userEmail = null;
    private static String userDisplayName = null;
    
    /**
     * Constructs a login dialog.
     * 
     * @param parentFrame The parent main frame
     */
    public LoginPanel(MainFrame parentFrame) {
        super(parentFrame, "Login", true); // Modal dialog
        this.parentFrame = parentFrame;
        
        // Set dialog properties
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setMinimumSize(new Dimension(350, 220));
        
        // Initialize components
        initComponents();
        
        // Pack and center on parent
        pack();
        setLocationRelativeTo(parentFrame);
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        // Create main panel with padding
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        registerButton = new JButton("Register");
        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");
        
        // Add action listeners
        loginButton.addActionListener(e -> login());
        cancelButton.addActionListener(e -> dispose());
        registerButton.addActionListener(e -> showRegisterDialog());
        
        // Add buttons to panel
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);
        
        // Add button panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(buttonPanel, gbc);
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
        
        // Set main content pane
        setContentPane(panel);
    }
    
    /**
     * Attempt to login with the provided credentials.
     */
    private void login() {
        String usernameText = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (usernameText.isEmpty()) {
            DialogFactory.showErrorDialog(this, "Error", "Username cannot be empty.");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            DialogFactory.showErrorDialog(this, "Error", "Password cannot be empty.");
            passwordField.requestFocus();
            return;
        }
        
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);
        cancelButton.setEnabled(false);
        registerButton.setEnabled(false);
        
        // Call API for authentication
        ApiClient.login(usernameText, password, response -> {
            setCursor(Cursor.getDefaultCursor());
            loginButton.setEnabled(true);
            cancelButton.setEnabled(true);
            registerButton.setEnabled(true);
            
            if (response.isSuccess()) {
                processAuthResponse(response.getData());
                dispose(); // Close dialog on success
            } else {
                DialogFactory.showErrorDialog(this, "Login Failed", 
                        "Authentication failed: " + response.getErrorMessage());
                passwordField.setText("");
                passwordField.requestFocus();
            }
            return null;
        });
    }
    
    /**
     * Process the authentication response from the API.
     * 
     * @param jsonResponse The JSON response data
     */
    private void processAuthResponse(JSONObject jsonResponse) {
        try {
            jwtToken = jsonResponse.getString("accessToken");
            userId = jsonResponse.getLong("id");
            username = jsonResponse.getString("username");
            userRole = jsonResponse.getString("role");
            userEmail = jsonResponse.optString("email", "");
            userDisplayName = jsonResponse.optString("displayName", username);
            
            // Update main frame
            parentFrame.updateLoginStatus(true);
            
            // Show welcome message
            DialogFactory.showInfoDialog(parentFrame, "Welcome", 
                    "Welcome, " + userDisplayName + "!");
        } catch (Exception e) {
            DialogFactory.showErrorDialog(this, "Error", 
                    "Error processing authentication response: " + e.getMessage());
            clearAuthentication();
        }
    }
    
    /**
     * Show the registration dialog.
     */
    private void showRegisterDialog() {
        // Create registration fields
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        JTextField emailField = new JTextField(15);
        JTextField displayNameField = new JTextField(15);
        
        // Create panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Display Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(displayNameField, gbc);
        
        // Show registration dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Register New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            // Validate input
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();
            String displayName = displayNameField.getText().trim();
            
            if (username.isEmpty() || username.length() < 3) {
                DialogFactory.showErrorDialog(this, "Validation Error", 
                        "Username must be at least 3 characters long.");
                return;
            }
            
            if (password.isEmpty() || password.length() < 8) {
                DialogFactory.showErrorDialog(this, "Validation Error", 
                        "Password must be at least 8 characters long.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                DialogFactory.showErrorDialog(this, "Validation Error", 
                        "Passwords do not match.");
                return;
            }
            
            if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
                DialogFactory.showErrorDialog(this, "Validation Error", 
                        "Please enter a valid email address.");
                return;
            }
            
            if (displayName.isEmpty() || displayName.length() < 2) {
                DialogFactory.showErrorDialog(this, "Validation Error", 
                        "Display name must be at least 2 characters long.");
                return;
            }
            
            // Register new user
            register(username, password, email, displayName);
        }
    }
    
    /**
     * Register a new user.
     * 
     * @param username Username
     * @param password Password
     * @param email Email
     * @param displayName Display name
     */
    private void register(String username, String password, String email, String displayName) {
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Call API for registration
        ApiClient.register(username, password, email, displayName, response -> {
            setCursor(Cursor.getDefaultCursor());
            
            if (response.isSuccess()) {
                DialogFactory.showInfoDialog(this, "Registration Successful", 
                        "User registered successfully. You can now log in.");
                
                // Auto-fill username in login form
                usernameField.setText(username);
                passwordField.setText("");
                passwordField.requestFocus();
            } else {
                DialogFactory.showErrorDialog(this, "Registration Failed", 
                        "Failed to register user: " + response.getErrorMessage());
            }
            return null;
        });
    }
    
    /**
     * Check if the user is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return jwtToken != null;
    }
    
    /**
     * Get the JWT token.
     * 
     * @return The JWT token or null if not authenticated
     */
    public static String getJwtToken() {
        return jwtToken;
    }
    
    /**
     * Get the authenticated user ID.
     * 
     * @return The user ID or null if not authenticated
     */
    public static Long getUserId() {
        return userId;
    }
    
    /**
     * Get the authenticated username.
     * 
     * @return The username or null if not authenticated
     */
    public static String getUsername() {
        return username;
    }
    
    /**
     * Get the authenticated user's role.
     * 
     * @return The user role or null if not authenticated
     */
    public static String getUserRole() {
        return userRole;
    }
    
    /**
     * Get the authenticated user's email.
     * 
     * @return The user email or empty string if not available
     */
    public static String getUserEmail() {
        return userEmail;
    }
    
    /**
     * Get the authenticated user's display name.
     * 
     * @return The user display name or empty string if not available
     */
    public static String getUserDisplayName() {
        return userDisplayName;
    }
    
    /**
     * Clear all authentication data (logout).
     */
    public static void clearAuthentication() {
        jwtToken = null;
        userId = null;
        username = null;
        userRole = null;
        userEmail = null;
        userDisplayName = null;
    }
}
