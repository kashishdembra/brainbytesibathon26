import java.sql.Timestamp;

/**
 * LoginAttempt Model - Represents a login attempt record
 */
public class LoginAttempt {
    private int id;
    private String username;
    private String ipAddress;
    private String status;
    private Timestamp attemptTime;
    private String userAgent;

    // Constructors
    public LoginAttempt() {}

    public LoginAttempt(String username, String ipAddress, String status) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAttemptTime() { return attemptTime; }
    public void setAttemptTime(Timestamp attemptTime) { this.attemptTime = attemptTime; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    @Override
    public String toString() {
        return "LoginAttempt{username='" + username + "', ip='" + ipAddress + "', status='" + status + "'}";
    }
}

