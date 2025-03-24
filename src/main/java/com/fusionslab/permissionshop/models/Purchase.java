package com.fusionslab.permissionshop.models;

import java.util.UUID;

/**
 * Represents a purchase made by a player
 */
public class Purchase {
    private final int id;
    private final UUID playerUuid;
    private final String itemId;
    private final long purchaseTime;
    private final long expirationTime;
    private int remainingUses;
    private boolean active;

    /**
     * Creates a new purchase with an ID (from database)
     *
     * @param id Database ID
     * @param playerUuid Player UUID
     * @param itemId Item ID
     * @param purchaseTime Purchase timestamp
     * @param expirationTime Expiration timestamp
     * @param remainingUses Remaining uses
     * @param active Whether the purchase is active
     */
    public Purchase(int id, UUID playerUuid, String itemId, long purchaseTime, long expirationTime, int remainingUses, boolean active) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.itemId = itemId;
        this.purchaseTime = purchaseTime;
        this.expirationTime = expirationTime;
        this.remainingUses = remainingUses;
        this.active = active;
    }

    /**
     * Creates a new purchase without an ID (not yet saved to database)
     *
     * @param playerUuid Player UUID
     * @param itemId Item ID
     * @param purchaseTime Purchase timestamp
     * @param expirationTime Expiration timestamp
     * @param remainingUses Remaining uses
     * @param active Whether the purchase is active
     */
    public Purchase(UUID playerUuid, String itemId, long purchaseTime, long expirationTime, int remainingUses, boolean active) {
        this(-1, playerUuid, itemId, purchaseTime, expirationTime, remainingUses, active);
    }

    /**
     * Gets the purchase ID
     *
     * @return Purchase ID
     */
    public int getId() {
        return id;
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
     * Gets the item ID
     *
     * @return Item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Gets the purchase timestamp
     *
     * @return Purchase timestamp
     */
    public long getPurchaseTime() {
        return purchaseTime;
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
     * Gets the remaining uses
     *
     * @return Remaining uses
     */
    public int getRemainingUses() {
        return remainingUses;
    }

    /**
     * Sets the remaining uses
     *
     * @param remainingUses Remaining uses
     */
    public void setRemainingUses(int remainingUses) {
        this.remainingUses = remainingUses;
    }

    /**
     * Decrements the remaining uses by 1
     *
     * @return New remaining uses count
     */
    public int decrementUses() {
        return --remainingUses;
    }

    /**
     * Checks if the purchase is active
     *
     * @return Whether the purchase is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the purchase is active
     *
     * @param active Whether the purchase is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Checks if the purchase has expired
     *
     * @return Whether the purchase has expired
     */
    public boolean hasExpired() {
        if (expirationTime == 0) {
            return false; // No expiration
        }
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Checks if the purchase has uses remaining
     *
     * @return Whether the purchase has uses remaining
     */
    public boolean hasUsesRemaining() {
        return remainingUses > 0 || remainingUses == -1; // -1 means unlimited
    }
}