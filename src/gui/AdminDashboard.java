import dataBase.DatabaseManager;
import logic.IPBlocker;
import model.User;
import model.LoginAttempt;
import model.BlockedIP;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Admin Dashboard - Main control panel for security monitoring
 * Features: Tabbed interface, real-time data, user management
 */
public class AdminDashboard extends JFrame {

    private User currentUser;
    private JTabbedPane tabbedPane;

    // Dashboard components
    private JLabel totalUsersLabel;
    private JLabel blockedIPsLabel;
    private JLabel failedAttemptsLabel;
    private JLabel successfulLoginsLabel;

    // Tables
    private JTable usersTable;
    private JTable attemptsTable;
    private JTable blockedIPsTable;

    // Table models
    private DefaultTableModel usersModel;
    private DefaultTableModel attemptsModel;
    private DefaultTableModel blockedIPsModel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        initializeWindow();
        createComponents();
        loadAllData();
        startAutoRefresh();
    }

    private void initializeWindow() {
        setTitle("🛡️ SIDRS Admin Dashboard - " + currentUser.getUsername());
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIStyles.BG_DARK);
    }

    private void createComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIStyles.BG_DARK);

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UIStyles.BG_DARK);
        tabbedPane.setForeground(UIStyles.TEXT_PRIMARY);
        tabbedPane.setFont(UIStyles.FONT_BODY);

        tabbedPane.addTab("📊 Dashboard", createDashboardPanel());
        tabbedPane.addTab("👥 Users", createUsersPanel());
        tabbedPane.addTab("📝 Login History", createAttemptsPanel());
        tabbedPane.addTab("🚫 Blocked IPs", createBlockedIPsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyles.BG_CARD);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Left side - Title
        JLabel title = new JLabel("🛡️ SIDRS Security Dashboard");
        title.setFont(UIStyles.FONT_HEADING);
        title.setForeground(UIStyles.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Right side - User info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(UIStyles.BG_CARD);

        JLabel userInfo = new JLabel("👤 " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userInfo.setFont(UIStyles.FONT_BODY);
        userInfo.setForeground(UIStyles.TEXT_SECONDARY);

        JButton refreshBtn = new JButton("🔄 Refresh");
        UIStyles.styleButton(refreshBtn, UIStyles.INFO);
        refreshBtn.addActionListener(e -> loadAllData());

        JButton logoutBtn = new JButton("🚪 Logout");
        UIStyles.styleButton(logoutBtn, UIStyles.DANGER);
        logoutBtn.addActionListener(e -> logout());

        rightPanel.add(userInfo);
        rightPanel.add(refreshBtn);
        rightPanel.add(logoutBtn);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIStyles.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Stats cards panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(UIStyles.BG_DARK);

        // Create stat cards
        totalUsersLabel = new JLabel("0");
        blockedIPsLabel = new JLabel("0");
        failedAttemptsLabel = new JLabel("0");
        successfulLoginsLabel = new JLabel("0");

        statsPanel.add(createStatCard("👥 Total Users", totalUsersLabel, UIStyles.INFO));
        statsPanel.add(createStatCard("🚫 Blocked IPs", blockedIPsLabel, UIStyles.DANGER));
        statsPanel.add(createStatCard("⚠️ Failed (24h)", failedAttemptsLabel, UIStyles.WARNING));
        statsPanel.add(createStatCard("✅ Success (24h)", successfulLoginsLabel, UIStyles.SUCCESS));

        panel.add(statsPanel, BorderLayout.NORTH);

        // Quick actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionsPanel.setBackground(UIStyles.BG_CARD);
        actionsPanel.setBorder(UIStyles.BORDER_CARD);

        JLabel actionsTitle = new JLabel("⚡ Quick Actions");
        actionsTitle.setFont(UIStyles.FONT_HEADING);
        actionsTitle.setForeground(UIStyles.TEXT_PRIMARY);
        actionsPanel.add(actionsTitle);

        JButton blockIPBtn = new JButton("🚫 Block IP");
        UIStyles.styleButton(blockIPBtn, UIStyles.DANGER);
        blockIPBtn.addActionListener(e -> showBlockIPDialog());
        actionsPanel.add(blockIPBtn);

        JButton viewLogsBtn = new JButton("📝 View Logs");
        UIStyles.styleButton(viewLogsBtn, UIStyles.PRIMARY);
        viewLogsBtn.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        actionsPanel.add(viewLogsBtn);

        panel.add(actionsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIStyles.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIStyles.FONT_BODY);
        titleLbl.setForeground(UIStyles.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        return card;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UIStyles.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table
        String[] columns = {"ID", "Username", "Role", "Status", "Created At", "Last Login"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usersTable = new JTable(usersModel);
        UIStyles.styleTable(usersTable);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.getViewport().setBackground(UIStyles.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyles.BG_CARD));

        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(UIStyles.BG_DARK);

        JButton lockBtn = new JButton("🔒 Lock User");
        UIStyles.styleButton(lockBtn, UIStyles.WARNING);
        lockBtn.addActionListener(e -> toggleUserStatus("LOCKED"));

        JButton unlockBtn = new JButton("🔓 Unlock User");
        UIStyles.styleButton(unlockBtn, UIStyles.SUCCESS);
        unlockBtn.addActionListener(e -> toggleUserStatus("ACTIVE"));

        buttonsPanel.add(lockBtn);
        buttonsPanel.add(unlockBtn);

        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAttemptsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UIStyles.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table
        String[] columns = {"ID", "Username", "IP Address", "Status", "Time"};
        attemptsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attemptsTable = new JTable(attemptsModel);
        UIStyles.styleTable(attemptsTable);

        // Custom cell renderer for status colors
        attemptsTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(attemptsTable);
        scrollPane.getViewport().setBackground(UIStyles.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyles.BG_CARD));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBlockedIPsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UIStyles.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table
        String[] columns = {"ID", "IP Address", "Blocked Time", "Expiry", "Reason", "Count", "Permanent"};
        blockedIPsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        blockedIPsTable = new JTable(blockedIPsModel);
        UIStyles.styleTable(blockedIPsTable);

        JScrollPane scrollPane = new JScrollPane(blockedIPsTable);
        scrollPane.getViewport().setBackground(UIStyles.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyles.BG_CARD));

        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(UIStyles.BG_DARK);

        JButton unblockBtn = new JButton("🔓 Unblock Selected");
        UIStyles.styleButton(unblockBtn, UIStyles.SUCCESS);
        unblockBtn.addActionListener(e -> unblockSelectedIP());

        JButton permanentBtn = new JButton("⛔ Make Permanent");
        UIStyles.styleButton(permanentBtn, UIStyles.DANGER);
        permanentBtn.addActionListener(e -> makePermanentBlock());

        buttonsPanel.add(unblockBtn);
        buttonsPanel.add(permanentBtn);

        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadAllData() {
        loadDashboardStats();
        loadUsers();
        loadLoginAttempts();
        loadBlockedIPs();
    }

    private void loadDashboardStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                return DatabaseManager.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    totalUsersLabel.setText(String.valueOf(stats[0]));
                    blockedIPsLabel.setText(String.valueOf(stats[1]));
                    failedAttemptsLabel.setText(String.valueOf(stats[2]));
                    successfulLoginsLabel.setText(String.valueOf(stats[3]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return DatabaseManager.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    usersModel.setRowCount(0);
                    for (User user : get()) {
                        usersModel.addRow(new Object[]{
                                user.getId(),
                                user.getUsername(),
                                user.getRole(),
                                user.getStatus(),
                                user.getCreatedAt(),
                                user.getLastLogin()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadLoginAttempts() {
        SwingWorker<List<LoginAttempt>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<LoginAttempt> doInBackground() {
                return DatabaseManager.getAllLoginAttempts();
            }

            @Override
            protected void done() {
                try {
                    attemptsModel.setRowCount(0);
                    for (LoginAttempt attempt : get()) {
                        attemptsModel.addRow(new Object[]{
                                attempt.getId(),
                                attempt.getUsername(),
                                attempt.getIpAddress(),
                                attempt.getStatus(),
                                attempt.getAttemptTime()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadBlockedIPs() {
        SwingWorker<List<BlockedIP>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BlockedIP> doInBackground() {
                return DatabaseManager.getAllBlockedIPs();
            }

            @Override
            protected void done() {
                try {
                    blockedIPsModel.setRowCount(0);
                    for (BlockedIP blocked : get()) {
                        blockedIPsModel.addRow(new Object[]{
                                blocked.getId(),
                                blocked.getIpAddress(),
                                blocked.getBlockedTime(),
                                blocked.getExpiryTime(),
                                blocked.getReason(),
                                blocked.getBlockCount(),
                                blocked.isPermanent() ? "Yes" : "No"
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void toggleUserStatus(String status) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select a user first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) usersModel.getValueAt(selectedRow, 0);
        String username = (String) usersModel.getValueAt(selectedRow, 1);

        if (DatabaseManager.updateUserStatus(userId, status)) {
            showMessage("User '" + username + "' status changed to " + status, "Success", JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } else {
            showMessage("Failed to update user status", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unblockSelectedIP() {
        int selectedRow = blockedIPsTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select an IP address first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ipAddress = (String) blockedIPsModel.getValueAt(selectedRow, 1);

        if (IPBlocker.unblockIP(ipAddress)) {
            showMessage("IP '" + ipAddress + "' has been unblocked", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBlockedIPs();
            loadDashboardStats();
        } else {
            showMessage("Failed to unblock IP", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makePermanentBlock() {
        int selectedRow = blockedIPsTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select an IP address first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ipAddress = (String) blockedIPsModel.getValueAt(selectedRow, 1);

        if (IPBlocker.setPermanentBlock(ipAddress, true)) {
            showMessage("IP '" + ipAddress + "' is now permanently blocked", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBlockedIPs();
        } else {
            showMessage("Failed to set permanent block", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBlockIPDialog() {
        String ip = JOptionPane.showInputDialog(this, "Enter IP address to block:", "Block IP", JOptionPane.QUESTION_MESSAGE);

        if (ip != null && !ip.trim().isEmpty()) {
            if (!IPBlocker.isValidIP(ip.trim())) {
                showMessage("Invalid IP address format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String reason = JOptionPane.showInputDialog(this, "Enter reason for blocking:", "Block Reason", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) reason = "Manually blocked by admin";

            if (IPBlocker.blockIP(ip.trim(), reason)) {
                showMessage("IP '" + ip + "' has been blocked", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBlockedIPs();
                loadDashboardStats();
            } else {
                showMessage("Failed to block IP", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginWindow().setVisible(true);
        }
    }

    private void startAutoRefresh() {
        // Auto-refresh every 30 seconds
        Timer timer = new Timer(30000, e -> loadAllData());
        timer.start();
    }

    // Custom cell renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && value != null) {
                String status = value.toString();
                setForeground(UIStyles.getStatusColor(status));
                setFont(UIStyles.FONT_BUTTON);
            }

            return c;
        }
    }
}

