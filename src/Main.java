import gui.LoginWindow;
import database.DatabaseManager;

import javax.swing.*;

/**
 * SIDRS - Security Intrusion Detection & Response System
 * Main entry point for the application
 *
 * @author SIDRS Team - Sibathon 2024
 */
public class Main {

    public static void main(String[] args) {  // FIXED: Removed <LoginWindow>

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            System.err.println("Failed to set Look & Feel");
        }

        // Check database connection before launching GUI
        if (!DatabaseManager.testConnection()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Database connection failed!\n\n" +
                            "Please ensure:\n" +
                            "1. MySQL is running\n" +
                            "2. Database 'sidrs_db' exists\n" +
                            "3. Credentials are correct",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(1);
        }

        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);  // FIXED: Changed notifyAll() to setVisible(true)
        });

        System.out.println("SIDRS - Security System Started");
    }
}