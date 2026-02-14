//package com.sidrs;

import com.sidrs.gui.LoginWindow;
import com.sidrs.database.DatabaseManager;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test database connection before starting GUI
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

        // Launch GUI safely
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });

        System.out.println("SIDRS - Security System Started");
    }
}
