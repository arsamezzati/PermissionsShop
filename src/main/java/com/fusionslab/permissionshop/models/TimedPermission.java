package com.fusionslab.permissionshop.models;

import java.util.UUID;

/**
 * Represents a permission that a player has for a limited time
 */
public class TimedPermission {
    private final UUID playerUuid;
    private final String permission;
    private final long expirationTime;
    private final int purchaseId;

    /**
     * Creates a new timed permission
     *
     * @param playerUuid Player UUID
     * @param permission Permission node
     * @param expirationTime Expiration timestamp
     * @param purchaseId ID of the purchase
     */
    public TimedPermission(UUID playerUuid, String permission, long expirationTime, int purchaseId) {
        this.playerUuid = playerUuid;
        this.permission = permission;
        this.expirationTime = expirationTime;
        this.purchaseId = purchaseId;
    }

    /**
     * Gets the player UUID
     *
     * @return Player UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Gets the permission node
     *
     * @return Permission node
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the expiration timestamp
     *
     * @return Expiration timestamp
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Gets the purchase ID
     *
     * @return Purchase ID
     */
    public int getPurchaseId() {
        return purchaseId;
    }

    /**
     * Checks if the permission has expired
     *
     * @return Whether the permission has expired
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Gets the time remaining in seconds
     *
     * @return Time remaining in seconds
     */
    public long getTimeRemaining() {
        long remaining = expirationTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }
}