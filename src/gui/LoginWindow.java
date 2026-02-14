package gui;

import database.DatabaseManager;
import logic.DetectionEngine;
import logic.DetectionEngine.DetectionResult;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;


public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField ipField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JLabel attemptsLabel;
    private JPanel mainPanel;

    private String currentIP;

    public LoginWindow() {
        initializeWindow();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        detectIP();
    }

    private void initializeWindow() {
        setTitle("🛡️ SIDRS - Security Login");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIStyles.BG_DARK);
    }

    private void createComponents() {
        // Main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIStyles.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Username field
        usernameField = new JTextField(20);
        UIStyles.styleTextField(usernameField);

        // Password field
        passwordField = new JPasswordField(20);
        UIStyles.stylePasswordField(passwordField);

        // IP field (read-only)
        ipField = new JTextField(20);
        UIStyles.styleTextField(ipField);
        ipField.setEditable(false);
        ipField.setBackground(UIStyles.BG_CARD);

        // Login button
        loginButton = new JButton("🔐 Login");
        UIStyles.styleButton(loginButton, UIStyles.PRIMARY);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyles.FONT_SMALL);
        statusLabel.setForeground(UIStyles.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Attempts label
        attemptsLabel = new JLabel(" ");
        attemptsLabel.setFont(UIStyles.FONT_SMALL);
        attemptsLabel.setForeground(UIStyles.TEXT_MUTED);
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void layoutComponents() {
        // Logo/Title
        JLabel titleLabel = new JLabel("🛡️ SIDRS");
        titleLabel.setFont(UIStyles.FONT_TITLE);
        titleLabel.setForeground(UIStyles.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Security Intrusion Detection System");
        subtitleLabel.setFont(UIStyles.FONT_SMALL);
        subtitleLabel.setForeground(UIStyles.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form labels
        JLabel userLabel = new JLabel("Username");
        UIStyles.styleLabel(userLabel, UIStyles.FONT_BODY, UIStyles.TEXT_SECONDARY);

        JLabel passLabel = new JLabel("Password");
        UIStyles.styleLabel(passLabel, UIStyles.FONT_BODY, UIStyles.TEXT_SECONDARY);

        JLabel ipLabel = new JLabel("Your IP Address");
        UIStyles.styleLabel(ipLabel, UIStyles.FONT_BODY, UIStyles.TEXT_SECONDARY);

        // Add components to panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        mainPanel.add(userLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(passLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(ipLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(ipField);
        mainPanel.add(Box.createVerticalStrut(30));

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        mainPanel.add(loginButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(attemptsLabel);

        add(mainPanel);
    }

    private void setupEventHandlers() {
        // Login button action
        loginButton.addActionListener(e -> performLogin());

        // Enter key in password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // Enter key in username field
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }

    private void detectIP() {
        try {
            currentIP = InetAddress.getLocalHost().getHostAddress();
            ipField.setText(currentIP);
        } catch (Exception e) {
            currentIP = "127.0.0.1";
            ipField.setText(currentIP);
        }

        // Check remaining attempts
        updateAttemptsLabel();
    }

    private void updateAttemptsLabel() {
        int remaining = DetectionEngine.getRemainingAttempts(currentIP);
        if (remaining < 3) {
            attemptsLabel.setText("⚠️ " + remaining + " attempts remaining before IP block");
            attemptsLabel.setForeground(UIStyles.WARNING);
        } else {
            attemptsLabel.setText(" ");
        }
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("⚠️ Please enter username and password", UIStyles.WARNING);
            return;
        }

        // Check if IP is blocked before attempting
        if (!DetectionEngine.canAttemptLogin(currentIP)) {
            showStatus("⛔ Your IP is blocked. Please try later.", UIStyles.DANGER);
            return;
        }

        // Disable button during login
        loginButton.setEnabled(false);
        loginButton.setText("🔄 Authenticating...");

        // Perform login in background
        SwingWorker<DetectionResult, Void> worker = new SwingWorker<>() {
            private User authenticatedUser;

            @Override
            protected DetectionResult doInBackground() {
                // Validate credentials
                authenticatedUser = DatabaseManager.validateUser(username, password);
                boolean success = authenticatedUser != null;

                // Analyze with detection engine
                return DetectionEngine.analyzeAttempt(currentIP, username, success);
            }

            @Override
            protected void done() {
                try {
                    DetectionResult result = get();

                    if (result.isBlocked()) {
                        showStatus(result.getMessage(), UIStyles.DANGER);
                        loginButton.setEnabled(true);
                        loginButton.setText("🔐 Login");
                    } else if (authenticatedUser != null) {
                        showStatus("✅ Login successful! Welcome, " + authenticatedUser.getUsername(), UIStyles.SUCCESS);

                        // Open dashboard after brief delay
                        Timer timer = new Timer(1000, e -> {
                            openDashboard(authenticatedUser);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus(result.getMessage(), UIStyles.DANGER);
                        updateAttemptsLabel();
                        loginButton.setEnabled(true);
                        loginButton.setText("🔐 Login");
                        passwordField.setText("");
                    }
                } catch (Exception e) {
                    showStatus("❌ Error: " + e.getMessage(), UIStyles.DANGER);
                    loginButton.setEnabled(true);
                    loginButton.setText("🔐 Login");
                }
            }
        };

        worker.execute();
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void openDashboard(User user) {
        dispose();
        AdminDashboard dashboard = new AdminDashboard(user);
        dashboard.setVisible(true);
    }
}
