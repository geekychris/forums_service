package com.forum.app;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Forum Management Application.
 */
public class ForumApp {
    
    /**
     * Application entry point.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Enable anti-aliasing for text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Create and show main window on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                } catch (Exception e) {
                    showErrorAndExit("Failed to start application", e);
                }
            });
        } catch (Exception e) {
            showErrorAndExit("Failed to initialize application", e);
        }
    }
    
    /**
     * Show error dialog and exit application.
     * 
     * @param message Error message
     * @param e Exception that occurred
     */
    private static void showErrorAndExit(String message, Exception e) {
        String fullMessage = message + "\n\nError: " + e.getMessage();
        JOptionPane.showMessageDialog(null, fullMessage,
                "Application Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
