import java.sql.Timestamp;

/**
 * BlockedIP Model - Represents a blocked IP address
 */
public class BlockedIP {
    private int id;
    private String ipAddress;
    private Timestamp blockedTime;
    private Timestamp expiryTime;
    private String reason;
    private int blockCount;
    private boolean isPermanent;

    // Constructors
    public BlockedIP() {}

    public BlockedIP(String ipAddress, String reason) {
        this.ipAddress = ipAddress;
        this.reason = reason;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getBlockedTime() { return blockedTime; }
    public void setBlockedTime(Timestamp blockedTime) { this.blockedTime = blockedTime; }

    public Timestamp getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Timestamp expiryTime) { this.expiryTime = expiryTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getBlockCount() { return blockCount; }
    public void setBlockCount(int blockCount) { this.blockCount = blockCount; }

    public boolean isPermanent() { return isPermanent; }
    public void setPermanent(boolean permanent) { isPermanent = permanent; }

    public boolean isExpired() {
        if (isPermanent) return false;
        if (expiryTime == null) return false;
        return expiryTime.before(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return "BlockedIP{ip='" + ipAddress + "', reason='" + reason + "', permanent=" + isPermanent + "}";
    }
}

