import dataBase.DatabaseManager;

/**
 * Detection Engine - Core intrusion detection logic
 * Monitors login attempts and triggers security responses
 */
public class DetectionEngine {

    // Configuration thresholds
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int TIME_WINDOW_MINUTES = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Analyze login attempt and determine if action is needed
     * @param ipAddress The IP address making the attempt
     * @param username The username attempted
     * @param success Whether the login was successful
     * @return Detection result with action to take
     */
    public static DetectionResult analyzeAttempt(String ipAddress, String username, boolean success) {
        DetectionResult result = new DetectionResult();

        // Check if IP is already blocked
        if (DatabaseManager.isIPBlocked(ipAddress)) {
            result.setBlocked(true);
            result.setMessage("⛔ This IP address is currently blocked");
            result.setThreatLevel(ThreatLevel.BLOCKED);

            // Record blocked attempt
            DatabaseManager.recordLoginAttempt(username, ipAddress, "BLOCKED");
            return result;
        }

        // Record the attempt
        String status = success ? "SUCCESS" : "FAILED";
        DatabaseManager.recordLoginAttempt(username, ipAddress, status);

        if (success) {
            result.setBlocked(false);
            result.setMessage("✅ Login successful");
            result.setThreatLevel(ThreatLevel.NONE);
            return result;
        }

        // Check failed attempt count for this IP
        int failedCount = DatabaseManager.getFailedAttemptsCount(ipAddress, TIME_WINDOW_MINUTES);

        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            // Block the IP
            String reason = String.format("Exceeded %d failed attempts in %d minutes",
                    MAX_FAILED_ATTEMPTS, TIME_WINDOW_MINUTES);
            IPBlocker.blockIP(ipAddress, reason);

            result.setBlocked(true);
            result.setMessage("🚨 IP blocked due to multiple failed attempts");
            result.setThreatLevel(ThreatLevel.HIGH);
            result.setFailedAttempts(failedCount);
        } else {
            result.setBlocked(false);
            result.setMessage(String.format("⚠️ Failed attempt %d of %d", failedCount, MAX_FAILED_ATTEMPTS));
            result.setThreatLevel(failedCount >= 2 ? ThreatLevel.MEDIUM : ThreatLevel.LOW);
            result.setFailedAttempts(failedCount);
        }

        return result;
    }

    /**
     * Check if an IP should be allowed to attempt login
     */
    public static boolean canAttemptLogin(String ipAddress) {
        return !DatabaseManager.isIPBlocked(ipAddress);
    }

    /**
     * Get remaining attempts before block
     */
    public static int getRemainingAttempts(String ipAddress) {
        int failed = DatabaseManager.getFailedAttemptsCount(ipAddress, TIME_WINDOW_MINUTES);
        return Math.max(0, MAX_FAILED_ATTEMPTS - failed);
    }

    // Inner classes for results
    public static class DetectionResult {
        private boolean blocked;
        private String message;
        private ThreatLevel threatLevel;
        private int failedAttempts;

        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ThreatLevel getThreatLevel() { return threatLevel; }
        public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }

        public int getFailedAttempts() { return failedAttempts; }
        public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    }

    public enum ThreatLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL, BLOCKED
    }
}