import dataBase.DatabaseManager;
import model.BlockedIP;

import java.util.List;

/**
 * IP Blocker - Manages IP blocking operations
 */
public class IPBlocker {

    /**
     * Block an IP address with reason
     */
    public static boolean blockIP(String ipAddress, String reason) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        boolean success = DatabaseManager.blockIP(ipAddress.trim(), reason);

        if (success) {
            System.out.println("🔒 IP Blocked: " + ipAddress + " - Reason: " + reason);
        }

        return success;
    }

    /**
     * Unblock an IP address
     */
    public static boolean unblockIP(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        boolean success = DatabaseManager.unblockIP(ipAddress.trim());

        if (success) {
            System.out.println("🔓 IP Unblocked: " + ipAddress);
        }

        return success;
    }

    /**
     * Check if IP is blocked
     */
    public static boolean isBlocked(String ipAddress) {
        return DatabaseManager.isIPBlocked(ipAddress);
    }

    /**
     * Get all blocked IPs
     */
    public static List<BlockedIP> getAllBlockedIPs() {
        return DatabaseManager.getAllBlockedIPs();
    }

    /**
     * Set permanent block on IP
     */
    public static boolean setPermanentBlock(String ipAddress, boolean permanent) {
        return DatabaseManager.setIPPermanentBlock(ipAddress, permanent);
    }

    /**
     * Validate IP address format (basic validation)
     */
    public static boolean isValidIP(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        // Basic IPv4 validation
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ipAddress.matches(ipPattern);
    }
}
