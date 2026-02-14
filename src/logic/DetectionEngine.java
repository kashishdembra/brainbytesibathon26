package com.sidrs.security.detection;

import com.sidrs.security.database.DatabaseManager;
import com.sidrs.security.security.IPBlocker;

/**
 * Detection Engine - Core intrusion detection logic
 */
public class DetectionEngine {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int TIME_WINDOW_MINUTES = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    public static DetectionResult analyzeAttempt(String ipAddress,
                                                 String username,
                                                 boolean success) {

        DetectionResult result = new DetectionResult();

        // Check if IP already blocked
        if (DatabaseManager.isIPBlocked(ipAddress)) {
            result.setBlocked(true);
            result.setMessage("IP address is currently blocked");
            result.setThreatLevel(ThreatLevel.BLOCKED);

            DatabaseManager.recordLoginAttempt(username, ipAddress, "BLOCKED");
            return result;
        }

        // Record login attempt
        String status = success ? "SUCCESS" : "FAILED";
        DatabaseManager.recordLoginAttempt(username, ipAddress, status);

        if (success) {
            result.setBlocked(false);
            result.setMessage("Login successful");
            result.setThreatLevel(ThreatLevel.NONE);
            return result;
        }

        // Count failed attempts
        int failedCount =
                DatabaseManager.getFailedAttemptsCount(ipAddress, TIME_WINDOW_MINUTES);

        if (failedCount >= MAX_FAILED_ATTEMPTS) {

            String reason = "Exceeded " + MAX_FAILED_ATTEMPTS +
                    " failed attempts in " + TIME_WINDOW_MINUTES + " minutes";

            IPBlocker.blockIP(ipAddress, reason, LOCKOUT_DURATION_MINUTES);

            result.setBlocked(true);
            result.setMessage("IP blocked due to multiple failed attempts");
            result.setThreatLevel(ThreatLevel.HIGH);
            result.setFailedAttempts(failedCount);
        } else {
            result.setBlocked(false);
            result.setMessage("Failed attempt " + failedCount +
                    " of " + MAX_FAILED_ATTEMPTS);
            result.setThreatLevel(
                    failedCount >= 2 ? ThreatLevel.MEDIUM : ThreatLevel.LOW
            );
            result.setFailedAttempts(failedCount);
        }

        return result;
    }

    public static boolean canAttemptLogin(String ipAddress) {
        return !DatabaseManager.isIPBlocked(ipAddress);
    }

    public static int getRemainingAttempts(String ipAddress) {
        int failed = DatabaseManager.getFailedAttemptsCount(ipAddress, TIME_WINDOW_MINUTES);
        return Math.max(0, MAX_FAILED_ATTEMPTS - failed);
    }

    // =============================
    // Inner Classes
    // =============================

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
        public void setThreatLevel(ThreatLevel threatLevel) {
            this.threatLevel = threatLevel;
        }

        public int getFailedAttempts() { return failedAttempts; }
        public void setFailedAttempts(int failedAttempts) {
            this.failedAttempts = failedAttempts;
        }
    }

    public enum ThreatLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL, BLOCKED
    }
}
