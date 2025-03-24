package com.fusionslab.permissionshop.models;

/**
 * Types of purchases available in the shop
 */
public enum PurchaseType {
    /**
     * Permission that lasts for a specified duration
     */
    TIMED_PERMISSION,

    /**
     * Command that can be used a limited number of times
     */
    LIMITED_COMMAND,

    /**
     * Permission that is permanent
     */
    PERMANENT_PERMISSION,

    /**
     * One-time command execution
     */
    ONE_TIME_COMMAND,

    /**
     * Increases the maximum number of homes a player can have
     */
    HOME_SLOT
}