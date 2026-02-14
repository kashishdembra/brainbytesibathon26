package database;

import model.user;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sidrs_db";
    private static final String DB_USER = "root";      // MySQL username
    private static final String DB_PASSWORD = "12345"; // MySQL password

    private static Connection connection;

    // ===============================
    // Get Connection
    // ===============================
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found");
            }
        }
        return connection;
    }

    // ===============================
    // Test Database Connection
    // ===============================
    public static boolean testConnection() {
        try {
            return getConnection() != null && !getConnection().isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // Validate User Login
    // ===============================
    public static User validateUser(String username, String password) {

        String sql = "SELECT * FROM users WHERE username=? AND password=? AND status='ACTIVE'";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, username);  // FIXED
            stmt.setString(2, password);  // FIXED

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ===============================
    // Record Login Attempt
    // ===============================
    public static void recordLoginAttempt(String username,
                                          String ipAddress,
                                          String status) {

        String sql = "INSERT INTO login_attempts (username, ip_address, status) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, ipAddress);
            stmt.setString(3, status);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===============================
    // Check if IP is Blocked
    // ===============================
    public static boolean isIPBlocked(String ipAddress) {

        String sql = "SELECT 1 FROM blocked_ips " +
                "WHERE ip_address=? AND (is_permanent=TRUE OR expiry_time > NOW())";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ===============================
    // Block IP
    // ===============================
    public static boolean blockIP(String ipAddress, String reason) {

        String sql = "INSERT INTO blocked_ips (ip_address, reason) VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, ipAddress);
            stmt.setString(2, reason);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // Count Failed Attempts
    // ===============================
    public static int getFailedAttemptsCount(String ipAddress, int minutes) {

        String sql = "SELECT COUNT(*) FROM login_attempts " +
                "WHERE ip_address=? AND status='FAILED' " +
                "AND attempt_time >= NOW() - INTERVAL ? MINUTE";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, ipAddress);
            stmt.setInt(2, minutes);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
