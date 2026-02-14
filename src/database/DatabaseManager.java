package database;

import model.User;
import model.LoginAttempt;
import model.BlockedIP;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager - Handles all JDBC operations with prepared statements
 * Implements secure database access patterns
 */
public class DatabaseManager {

    // Database configuration - Update these for your setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sidrs_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345"; // Update with your MySQL password

    private static Connection connection;

    /**
     * Get database connection (singleton pattern)
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("✅ Database connected successfully");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Validate user credentials - SECURE with prepared statement
     */
    public static User validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));

                // Update last login time
                updateLastLogin(user.getId());

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update user's last login timestamp
     */
    private static void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Get all users
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Update user status (lock/unlock)
     */
    public static boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if user exists
     */
    public static boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    // ==================== LOGIN ATTEMPT OPERATIONS ====================

    /**
     * Record a login attempt - SECURE with prepared statement
     */
    public static boolean recordLoginAttempt(String username, String ipAddress, String status) {
        String sql = "INSERT INTO login_attempts (username, ip_address, status) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, ipAddress);
            stmt.setString(3, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording login attempt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Count failed attempts from an IP in the last X minutes
     */
    public static int getFailedAttemptsCount(String ipAddress, int minutes) {
        String sql = "SELECT COUNT(*) FROM login_attempts " +
                "WHERE ip_address = ? AND status = 'FAILED' " +
                "AND attempt_time > DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setInt(2, minutes);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting failed attempts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get all login attempts
     */
    public static List<LoginAttempt> getAllLoginAttempts() {
        List<LoginAttempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM login_attempts ORDER BY attempt_time DESC LIMIT 100";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoginAttempt attempt = new LoginAttempt();
                attempt.setId(rs.getInt("id"));
                attempt.setUsername(rs.getString("username"));
                attempt.setIpAddress(rs.getString("ip_address"));
                attempt.setStatus(rs.getString("status"));
                attempt.setAttemptTime(rs.getTimestamp("attempt_time"));
                attempts.add(attempt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching login attempts: " + e.getMessage());
        }
        return attempts;
    }

    /**
     * Get recent login attempts (last 24 hours)
     */
    public static List<LoginAttempt> getRecentLoginAttempts() {
        List<LoginAttempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM login_attempts " +
                "WHERE attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
                "ORDER BY attempt_time DESC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoginAttempt attempt = new LoginAttempt();
                attempt.setId(rs.getInt("id"));
                attempt.setUsername(rs.getString("username"));
                attempt.setIpAddress(rs.getString("ip_address"));
                attempt.setStatus(rs.getString("status"));
                attempt.setAttemptTime(rs.getTimestamp("attempt_time"));
                attempts.add(attempt);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recent attempts: " + e.getMessage());
        }
        return attempts;
    }

    // ==================== BLOCKED IP OPERATIONS ====================

    /**
     * Block an IP address - SECURE with prepared statement
     */
    public static boolean blockIP(String ipAddress, String reason) {
        // First check if IP is already blocked
        if (isIPBlocked(ipAddress)) {
            // Update block count
            String updateSql = "UPDATE blocked_ips SET block_count = block_count + 1, " +
                    "blocked_time = CURRENT_TIMESTAMP, reason = ? WHERE ip_address = ?";
            try (PreparedStatement stmt = getConnection().prepareStatement(updateSql)) {
                stmt.setString(1, reason);
                stmt.setString(2, ipAddress);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error updating blocked IP: " + e.getMessage());
                return false;
            }
        }

        // Insert new blocked IP
        String sql = "INSERT INTO blocked_ips (ip_address, reason, expiry_time) " +
                "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 30 MINUTE))";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setString(2, reason);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error blocking IP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if an IP is currently blocked
     */
    public static boolean isIPBlocked(String ipAddress) {
        String sql = "SELECT COUNT(*) FROM blocked_ips " +
                "WHERE ip_address = ? AND (expiry_time IS NULL OR expiry_time > NOW() OR is_permanent = TRUE)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking blocked IP: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all blocked IPs
     */
    public static List<BlockedIP> getAllBlockedIPs() {
        List<BlockedIP> blockedIPs = new ArrayList<>();
        String sql = "SELECT * FROM blocked_ips ORDER BY blocked_time DESC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BlockedIP blocked = new BlockedIP();
                blocked.setId(rs.getInt("id"));
                blocked.setIpAddress(rs.getString("ip_address"));
                blocked.setBlockedTime(rs.getTimestamp("blocked_time"));
                blocked.setExpiryTime(rs.getTimestamp("expiry_time"));
                blocked.setReason(rs.getString("reason"));
                blocked.setBlockCount(rs.getInt("block_count"));
                blocked.setPermanent(rs.getBoolean("is_permanent"));
                blockedIPs.add(blocked);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching blocked IPs: " + e.getMessage());
        }
        return blockedIPs;
    }

    /**
     * Unblock an IP address
     */
    public static boolean unblockIP(String ipAddress) {
        String sql = "DELETE FROM blocked_ips WHERE ip_address = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error unblocking IP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set IP block as permanent
     */
    public static boolean setIPPermanentBlock(String ipAddress, boolean permanent) {
        String sql = "UPDATE blocked_ips SET is_permanent = ?, expiry_time = NULL WHERE ip_address = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, permanent);
            stmt.setString(2, ipAddress);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating permanent block: " + e.getMessage());
            return false;
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get dashboard statistics
     */
    public static int[] getDashboardStats() {
        int[] stats = new int[4]; // [totalUsers, blockedIPs, failedAttempts24h, successfulLogins24h]

        try {
            // Total users
            String sql1 = "SELECT COUNT(*) FROM users";
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql1)) {
                if (rs.next()) stats[0] = rs.getInt(1);
            }

            // Blocked IPs
            String sql2 = "SELECT COUNT(*) FROM blocked_ips WHERE expiry_time > NOW() OR is_permanent = TRUE";
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql2)) {
                if (rs.next()) stats[1] = rs.getInt(1);
            }

            // Failed attempts in 24h
            String sql3 = "SELECT COUNT(*) FROM login_attempts WHERE status = 'FAILED' " +
                    "AND attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)";
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql3)) {
                if (rs.next()) stats[2] = rs.getInt(1);
            }

            // Successful logins in 24h
            String sql4 = "SELECT COUNT(*) FROM login_attempts WHERE status = 'SUCCESS' " +
                    "AND attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)";
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql4)) {
                if (rs.next()) stats[3] = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}


