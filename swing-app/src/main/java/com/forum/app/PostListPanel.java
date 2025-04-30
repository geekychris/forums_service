package com.forum.app;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel showing posts from the selected forum.
 */
public class PostListPanel extends JPanel {
    
    private final MainFrame parentFrame;
    private JTable postTable;
    private DefaultTableModel tableModel;
    private JTextArea postContentArea;
    private JSplitPane splitPane;
    private JToolBar toolBar;
    private JButton newPostButton;
    private JPopupMenu popupMenu;
    
    // Current context
    private Long currentForumId;
    private Map<Integer, PostDto> postCache = new HashMap<>();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Constructs the post list panel.
     * 
     * @param parentFrame The parent main frame
     */
    public PostListPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Posts"));
        
        initComponents();
        initPopupMenu();
        initListeners();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        // Create toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        newPostButton = new JButton("New Post");
        newPostButton.setToolTipText("Create a new post");
        newPostButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        newPostButton.setEnabled(false); // Disabled until a forum is selected
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh post list");
        refreshButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        
        JButton viewCommentsButton = new JButton("Comments");
        viewCommentsButton.setToolTipText("View comments for the selected post");
        viewCommentsButton.setIcon(UIManager.getIcon("Tree.openIcon"));
        viewCommentsButton.setEnabled(false); // Disabled until a post is selected
        
        // Add buttons to toolbar
        toolBar.add(newPostButton);
        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(viewCommentsButton);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Create table model with column names
        String[] columnNames = {"Title", "Author", "Date", "Comments"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 3: // Comments column
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
        
        // Create table
        postTable = new JTable(tableModel);
        postTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postTable.setRowHeight(24);
        postTable.setShowGrid(false);
        postTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Set column widths
        TableColumnModel columnModel = postTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(300); // Title
        columnModel.getColumn(1).setPreferredWidth(100); // Author
        columnModel.getColumn(2).setPreferredWidth(120); // Date
        columnModel.getColumn(3).setPreferredWidth(80);  // Comments
        
        // Create content area
        postContentArea = new JTextArea();
        postContentArea.setEditable(false);
        postContentArea.setLineWrap(true);
        postContentArea.setWrapStyleWord(true);
        postContentArea.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
        postContentArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Create split pane with post list and content
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(postTable),
                new JScrollPane(postContentArea));
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.4);
        
        // Add split pane to panel
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Initialize the popup menu.
     */
    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        
        JMenuItem viewItem = new JMenuItem("View Post");
        viewItem.setIcon(UIManager.getIcon("Tree.leafIcon"));
        
        JMenuItem editItem = new JMenuItem("Edit Post");
        editItem.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        
        JMenuItem deleteItem = new JMenuItem("Delete Post");
        deleteItem.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
        
        JMenuItem commentsItem = new JMenuItem("View Comments");
        commentsItem.setIcon(UIManager.getIcon("Tree.openIcon"));
        
        // Add action listeners
        viewItem.addActionListener(e -> displaySelectedPost());
        editItem.addActionListener(e -> editSelectedPost());
        deleteItem.addActionListener(e -> deleteSelectedPost());
        commentsItem.addActionListener(e -> viewCommentsForSelectedPost());
        
        // Add items to popup menu
        popupMenu.add(viewItem);
        popupMenu.add(editItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);
        popupMenu.addSeparator();
        popupMenu.add(commentsItem);
    }
    
    /**
     * Initialize event listeners.
     */
    private void initListeners() {
        // Table selection listener
        postTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = postTable.getSelectedRow() != -1;
                JButton commentsButton = (JButton) toolBar.getComponent(3);
                commentsButton.setEnabled(hasSelection);
                
                displaySelectedPost();
            }
        });
        
        // Mouse listener for popup menu
        postTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }
            
            private void handleMouseEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = postTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        postTable.setRowSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else if (e.getClickCount() == 2) {
                    int row = postTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        displaySelectedPost();
                    }
                }
            }
        });
        
        // Button listeners
        newPostButton.addActionListener(e -> createNewPost());
        JButton refreshButton = (JButton) toolBar.getComponent(1);
        refreshButton.addActionListener(e -> refreshData());
        JButton commentsButton = (JButton) toolBar.getComponent(3);
        commentsButton.addActionListener(e -> viewCommentsForSelectedPost());
    }
    
    /**
     * Display the content of the selected post.
     */
    private void displaySelectedPost() {
        int selectedRow = postTable.getSelectedRow();
        if (selectedRow >= 0) {
            PostDto post = postCache.get(selectedRow);
            if (post != null) {
                String content = String.format("Title: %s\n\nAuthor: %s\nDate: %s\n\n%s",
                        post.getTitle(),
                        post.getAuthor().getDisplayName(),
                        DATE_FORMATTER.format(post.getCreatedAt()),
                        post.getContent());
                postContentArea.setText(content);
                postContentArea.setCaretPosition(0);
            }
        } else {
            postContentArea.setText("");
        }
    }
    
    /**
     * Create a new post in the current forum.
     */
    private void createNewPost() {
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", 
                    "You must be logged in to create posts.");
            return;
        }
        
        if (currentForumId == null) {
            DialogFactory.showErrorDialog(this, "Error", "No forum selected.");
            return;
        }
        
        DialogFactory.showCreatePostDialog(parentFrame, currentForumId);
        refreshData();
    }
    
    /**
     * Edit the selected post.
     */
    private void editSelectedPost() {
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", 
                    "You must be logged in to edit posts.");
            return;
        }
        
        int selectedRow = postTable.getSelectedRow();
        if (selectedRow >= 0) {
            PostDto post = postCache.get(selectedRow);
            if (post != null) {
                if (!post.isCanEdit()) {
                    DialogFactory.showErrorDialog(this, "Error", 
                            "You don't have permission to edit this post.");
                    return;
                }
                
                DialogFactory.showEditPostDialog(parentFrame, post);
                refreshData();
            }
        }
    }
    
    /**
     * Delete the selected post.
     */
    private void deleteSelectedPost() {
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", 
                    "You must be logged in to delete posts.");
            return;
        }
        
        int selectedRow = postTable.getSelectedRow();
        if (selectedRow >= 0) {
            PostDto post = postCache.get(selectedRow);
            if (post != null) {
                if (!post.isCanDelete()) {
                    DialogFactory.showErrorDialog(this, "Error", 
                            "You don't have permission to delete this post.");
                    return;
                }
                
                boolean confirmed = DialogFactory.showConfirmDialog(this, "Confirm Deletion",
                        "Are you sure you want to delete the post \"" + post.getTitle() + "\"?\n" +
                        "This will also delete all comments.");
                
                if (confirmed) {
                    ApiClient.deletePost(post.getId(), response -> {
                        if (response.isSuccess()) {
                            DialogFactory.showInfoDialog(this, "Success", "Post deleted successfully.");
                            refreshData();
                        } else {
                            DialogFactory.showErrorDialog(this, "Error", 
                                    "Failed to delete post: " + response.getErrorMessage());
                        }
                        return null;
                    });
                }
            }
        }
    }
    
    /**
     * View comments for the selected post.
     */
    private void viewCommentsForSelectedPost() {
        int selectedRow = postTable.getSelectedRow();
        if (selectedRow >= 0) {
            PostDto post = postCache.get(selectedRow);
            if (post != null) {
                // TODO: Implement comment view dialog
                DialogFactory.showInfoDialog(this, "Not Implemented", 
                        "Comment viewing will be implemented in a future update.");
            }
        }
    }
    
    /**
     * Refresh the post list data.
     */
    public void refreshData() {
        refreshData(currentForumId);
    }
    
    /**
     * Refresh the post list data for a specific forum.
     * 
     * @param forumId Forum ID to load posts from
     */
    public void refreshData(Long forumId) {
        this.currentForumId = forumId;
        newPostButton.setEnabled(forumId != null);
        
        // Clear existing data
        tableModel.setRowCount(0);
        postCache.clear();
        postContentArea.setText("");
        
        if (forumId != null) {
            ApiClient.getPostsByForum(forumId, currentPage, PAGE_SIZE, response -> {
                if (response.isSuccess()) {
                    List<PostDto> posts = response.getData();
                    for (int i = 0; i < posts.size(); i++) {
                        PostDto post = posts.get(i);
                        postCache.put(i, post);
                        
                        Object[] rowData = {
                            post.getTitle(),
                            post.getAuthor().getDisplayName(),
                            DATE_FORMATTER.format(post.getCreatedAt()),
                            post.getCommentCount()
                        };
                        tableModel.addRow(rowData);
                    }
                } else {
                    DialogFactory.showErrorDialog(this, "Error", 
                            "Failed to load posts: " + response.getErrorMessage());
                }
                return null;
            });
        }
    }
    
    /**
     * Clear all posts from the panel.
     */
    public void clearPosts() {
        // Clear existing data
        currentForumId = null;
        tableModel.setRowCount(0);
        postCache.clear();
        postContentArea.setText("");
        newPostButton.setEnabled(false);
    }
}
