package com.forum.app;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

/**
 * Panel showing a hierarchical tree view of forums.
 * Allows for creating, editing, and deleting forums through a context menu.
 */
public class ForumTreePanel extends JPanel {
    
    private final MainFrame parentFrame;
    private JTree forumTree;
    private DefaultTreeModel treeModel;
    private JPopupMenu popupMenu;
    private JButton refreshButton;
    private JButton addRootForumButton;
    
    /**
     * Constructs the forum tree panel.
     * 
     * @param parentFrame The parent main frame
     */
    public ForumTreePanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Forums"));
        
        initComponents();
        initPopupMenu();
        initListeners();
        
        // Initial data load
        refreshData();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        // Create root node
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ForumTreeNode(null, "Forums", "Root node for all forums"));
        treeModel = new DefaultTreeModel(rootNode);
        
        // Create tree and set properties
        forumTree = new JTree(treeModel);
        forumTree.setRootVisible(true);
        forumTree.setShowsRootHandles(true);
        forumTree.setCellRenderer(new ForumTreeCellRenderer());
        forumTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // Add components to panel
        JScrollPane scrollPane = new JScrollPane(forumTree);
        scrollPane.setPreferredSize(new Dimension(250, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Add toolbar for actions
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh forum list");
        refreshButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        
        addRootForumButton = new JButton("New Forum");
        addRootForumButton.setToolTipText("Add new top-level forum");
        addRootForumButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        
        toolBar.add(refreshButton);
        toolBar.add(addRootForumButton);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * Initialize popup menu with forum operations.
     */
    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        
        JMenuItem addSubforumItem = new JMenuItem("Add Subforum");
        addSubforumItem.setIcon(UIManager.getIcon("FileView.fileIcon"));
        
        JMenuItem editForumItem = new JMenuItem("Edit Forum");
        editForumItem.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        
        JMenuItem deleteForumItem = new JMenuItem("Delete Forum");
        deleteForumItem.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
        
        addSubforumItem.addActionListener(e -> addSubforum());
        editForumItem.addActionListener(e -> editForum());
        deleteForumItem.addActionListener(e -> deleteForum());
        
        popupMenu.add(addSubforumItem);
        popupMenu.add(editForumItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteForumItem);
    }
    
    /**
     * Initialize event listeners.
     */
    private void initListeners() {
        // Refresh button action
        refreshButton.addActionListener(e -> refreshData());
        
        // Add root forum button action
        addRootForumButton.addActionListener(e -> addRootForum());
        
        // Tree selection listener - update post panel when a forum is selected
        forumTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) forumTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof ForumTreeNode) {
                ForumTreeNode forumNode = (ForumTreeNode) node.getUserObject();
                if (forumNode.getId() != null) {
                    // Update post list with posts from this forum
                    parentFrame.getPostPanel().refreshData(forumNode.getId());
                }
            }
        });
        
        // Tree right-click listener for popup menu
        forumTree.addMouseListener(new MouseAdapter() {
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
                    // Get the node under the cursor
                    TreePath path = forumTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        forumTree.setSelectionPath(path);
                        
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object userObject = node.getUserObject();
                        
                        // Only show popup for forum nodes (not the root "Forums" node)
                        if (userObject instanceof ForumTreeNode && ((ForumTreeNode) userObject).getId() != null) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Refresh forum data from the server.
     */
    public void refreshData() {
        // Clear existing nodes except root
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.removeAllChildren();
        
        // Get root forums from API
        ApiClient.getRootForums(response -> {
            if (response.isSuccess()) {
                // Add forums to tree
                addForumsToTree(rootNode, response.getData());
                treeModel.reload();
                
                // Expand all nodes for better visibility
                expandAllNodes(forumTree, 0, forumTree.getRowCount());
            } else {
                DialogFactory.showErrorDialog(this, "Error", "Failed to fetch forums: " + response.getErrorMessage());
            }
            return null;
        });
    }
    
    /**
     * Add forums to the tree under the specified parent node.
     * 
     * @param parentNode Parent tree node
     * @param forums List of forums to add
     */
    private void addForumsToTree(DefaultMutableTreeNode parentNode, List<ForumDto> forums) {
        for (ForumDto forum : forums) {
            ForumTreeNode forumNode = new ForumTreeNode(forum.getId(), forum.getName(), forum.getDescription());
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(forumNode);
            parentNode.add(treeNode);
            
            // Add subforums recursively
            if (forum.getSubForums() != null && !forum.getSubForums().isEmpty()) {
                addForumsToTree(treeNode, forum.getSubForums());
            }
        }
    }
    
    /**
     * Expand all nodes in the tree.
     * 
     * @param tree The JTree
     * @param startingIndex Starting row index
     * @param rowCount Total row count
     */
    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
    
    /**
     * Get the selected forum ID.
     * 
     * @return The selected forum ID or null if none selected
     */
    private Long getSelectedForumId() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) forumTree.getLastSelectedPathComponent();
        if (node != null && node.getUserObject() instanceof ForumTreeNode) {
            ForumTreeNode forumNode = (ForumTreeNode) node.getUserObject();
            return forumNode.getId();
        }
        return null;
    }
    
    /**
     * Get the selected forum tree node.
     * 
     * @return The selected forum tree node or null if none selected
     */
    private ForumTreeNode getSelectedForumNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) forumTree.getLastSelectedPathComponent();
        if (node != null && node.getUserObject() instanceof ForumTreeNode) {
            return (ForumTreeNode) node.getUserObject();
        }
        return null;
    }
    
    /**
     * Add a new root-level forum.
     */
    private void addRootForum() {
        // Check if user is authenticated
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", "You must be logged in to create forums.");
            return;
        }
        
        // Show create forum dialog
        DialogFactory.showCreateForumDialog(parentFrame, null);
        
        // Refresh data after dialog closes
        refreshData();
    }
    
    /**
     * Add a subforum to the selected forum.
     */
    private void addSubforum() {
        // Check if user is authenticated
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", "You must be logged in to create forums.");
            return;
        }
        
        Long parentId = getSelectedForumId();
        if (parentId == null) {
            DialogFactory.showErrorDialog(this, "Error", "No parent forum selected.");
            return;
        }
        
        // Show create forum dialog
        DialogFactory.showCreateForumDialog(parentFrame, parentId);
        
        // Refresh data after dialog closes
        refreshData();
    }
    
    /**
     * Edit the selected forum.
     */
    private void editForum() {
        // Check if user is authenticated
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", "You must be logged in to edit forums.");
            return;
        }
        
        Long forumId = getSelectedForumId();
        if (forumId == null) {
            DialogFactory.showErrorDialog(this, "Error", "No forum selected for editing.");
            return;
        }
        
        // Get forum data from API
        ApiClient.getForumById(forumId, response -> {
            if (response.isSuccess()) {
                DialogFactory.showEditForumDialog(parentFrame, response.getData());
                refreshData();
            } else {
                DialogFactory.showErrorDialog(this, "Error", "Failed to get forum details: " + response.getErrorMessage());
            }
            return null;
        });
    }
    
    /**
     * Delete the selected forum.
     */
    private void deleteForum() {
        // Check if user is authenticated
        if (!LoginPanel.isAuthenticated()) {
            DialogFactory.showErrorDialog(this, "Authentication Required", "You must be logged in to delete forums.");
            return;
        }
        
        Long forumId = getSelectedForumId();
        ForumTreeNode forumNode = getSelectedForumNode();
        
        if (forumId == null || forumNode == null) {
            DialogFactory.showErrorDialog(this, "Error", "No forum selected for deletion.");
            return;
        }
        
        // Confirm deletion
        boolean confirmed = DialogFactory.showConfirmDialog(this, "Confirm Deletion", 
                "Are you sure you want to delete the forum \"" + forumNode.getName() + "\"?\n" +
                "This will delete all posts and subforums within it.");
        
        if (confirmed) {
            // Delete forum via API
            ApiClient.deleteForum(forumId, response -> {
                if (response.isSuccess()) {
                    DialogFactory.showInfoDialog(this, "Success", "Forum deleted successfully.");
                    refreshData(); // Refresh tree to reflect changes
                } else {
                    DialogFactory.showErrorDialog(this, "Error", "Failed to delete forum: " + response.getErrorMessage());
                }
                return null;
            });
        }
    }
    
    /**
     * Custom tree node class for forums.
     */
    private static class ForumTreeNode {
        private final Long id;
        private final String name;
        private final String description;
        
        public ForumTreeNode(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Custom cell renderer for forum tree nodes.
     */
    private static class ForumTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof ForumTreeNode) {
                ForumTreeNode forumNode = (ForumTreeNode) userObject;
                
                if (forumNode.getId() == null) {
                    // Root node
                    setIcon(UIManager.getIcon("FileView.computerIcon"));
                } else if (!node.isLeaf()) {
                    // Forum with subforums
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                } else {
                    // Forum without subforums
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
                
                // Set tooltip to show description
                setToolTipText(forumNode.getDescription());
            }
            
            return this;
        }
    }
}

