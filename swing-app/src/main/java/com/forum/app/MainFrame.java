package com.forum.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main window of the Forum Management Swing application.
 * This frame contains the main layout with split panes for forums and posts.
 */
public class MainFrame extends JFrame {
    
    private ForumTreePanel forumPanel;
    private PostListPanel postPanel;
    private JLabel statusLabel;
    private JSplitPane splitPane;
    
    private JMenuItem loginItem;
    private JMenuItem logoutItem;
    
    /**
     * Constructs the main application window.
     */
    public MainFrame() {
        super("Forum Management Application");
        
        // Set up window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setPreferredSize(new Dimension(1200, 800));
        
        // Initialize UI components
        initComponents();
        initMenus();
        updateLoginStatus(false);
        
        // Handle window events
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                showLoginDialog();
            }
        });
        
        // Pack and center on screen
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        // Create main content panels
        forumPanel = new ForumTreePanel(this);
        postPanel = new PostListPanel(this);
        
        // Create split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, forumPanel, postPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.3);
        
        // Add to content pane
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Not logged in");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Initialize menu bar and menu items.
     */
    private void initMenus() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        loginItem = new JMenuItem("Login");
        loginItem.setMnemonic('L');
        loginItem.addActionListener(e -> showLoginDialog());
        
        logoutItem = new JMenuItem("Logout");
        logoutItem.setMnemonic('O');
        logoutItem.addActionListener(e -> logout());
        logoutItem.setEnabled(false);
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loginItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setMnemonic('R');
        refreshItem.addActionListener(e -> refreshAll());
        viewMenu.add(refreshItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        // Set menu bar
        setJMenuBar(menuBar);
    }
    
    /**
     * Shows the login dialog.
     */
    public void showLoginDialog() {
        if (!LoginPanel.isAuthenticated()) {
            LoginPanel loginPanel = new LoginPanel(this);
            loginPanel.setVisible(true);
        }
    }
    
    /**
     * Updates the login status display and menu items.
     * 
     * @param loggedIn true if user is logged in, false otherwise
     */
    public void updateLoginStatus(boolean loggedIn) {
        // Update menu items
        loginItem.setEnabled(!loggedIn);
        logoutItem.setEnabled(loggedIn);
        
        // Update status bar
        if (loggedIn) {
            String username = LoginPanel.getUsername();
            String role = LoginPanel.getUserRole();
            statusLabel.setText("Logged in as: " + username + " (" + role + ")");
        } else {
            statusLabel.setText("Not logged in");
        }
        
        // Refresh views to reflect login state
        refreshAll();
    }
    
    /**
     * Logs out the current user.
     */
    private void logout() {
        if (DialogFactory.showConfirmDialog(this, "Confirm Logout", "Are you sure you want to log out?")) {
            LoginPanel.clearAuthentication();
            updateLoginStatus(false);
            DialogFactory.showInfoDialog(this, "Logged Out", "You have been logged out successfully.");
        }
    }
    
    /**
     * Refreshes all panels to show current data.
     */
    public void refreshAll() {
        forumPanel.refreshData();
        postPanel.clearPosts(); // Clear posts when refreshing all (no forum selected)
    }
    
    /**
     * Shows the about dialog.
     */
    private void showAboutDialog() {
        DialogFactory.showInfoDialog(this, "About Forum Application",
                "Forum Management Application\n" +
                "Version 1.0\n\n" +
                "A Swing application for managing forums, posts, and comments.\n" +
                "Uses REST API backend at http://localhost:9090");
    }
    
    /**
     * Get the forum panel.
     * 
     * @return The forum tree panel
     */
    public ForumTreePanel getForumPanel() {
        return forumPanel;
    }
    
    /**
     * Get the post panel.
     * 
     * @return The post list panel
     */
    public PostListPanel getPostPanel() {
        return postPanel;
    }
}
