package com.fusionslab.permissionshop.models;

/**
 * Represents an item in the permissions shop
 */
public class ShopItem {
    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final PurchaseType type;
    private final int duration; // In seconds for timed permissions
    private final int uses; // For limited commands
    private final String permission; // Permission node
    private final String command; // Command to execute
    private final boolean displayInShop;

    /**
     * Creates a new shop item
     *
     * @param id Unique identifier
     * @param name Display name
     * @param description Item description
     * @param price Cost in economy
     * @param type Type of purchase
     * @param duration Duration in seconds (for timed permissions)
     * @param uses Number of uses (for limited commands)
     * @param permission Permission node
     * @param command Command to execute
     * @param displayInShop Whether to display in shop
     */
    public ShopItem(String id, String name, String description, double price, PurchaseType type,
                    int duration, int uses, String permission, String command, boolean displayInShop) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.duration = duration;
        this.uses = uses;
        this.permission = permission;
        this.command = command;
        this.displayInShop = displayInShop;
    }

    /**
     * Gets the item ID
     *
     * @return Item ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the item name
     *
     * @return Item name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the item description
     *
     * @return Item description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the item price
     *
     * @return Item price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the purchase type
     *
     * @return Purchase type
     */
    public PurchaseType getType() {
        return type;
    }

    /**
     * Gets the duration in seconds
     *
     * @return Duration in seconds
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Gets the number of uses
     *
     * @return Number of uses
     */
    public int getUses() {
        return uses;
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
     * Gets the command to execute
     *
     * @return Command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Checks if the item should be displayed in the shop
     *
     * @return Whether to display in shop
     */
    public boolean isDisplayInShop() {
        return displayInShop;
    }
}