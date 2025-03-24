package com.fusionslab.permissionshop.managers;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.Purchase;
import com.fusionslab.permissionshop.models.PurchaseType;
import com.fusionslab.permissionshop.models.ShopItem;
import com.fusionslab.permissionshop.models.TimedPermission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages player purchases with LuckPerms integration
 */
public class PurchaseManager {

    private final PermissionsShop plugin;

    public PurchaseManager(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Makes a purchase
     *
     * @param player Player making the purchase
     * @param itemId ID of the item to purchase
     * @return Whether the purchase was successful
     */
    public boolean makePurchase(Player player, String itemId) {
        ShopItem item = plugin.getConfigManager().getShopItem(itemId);

        if (item == null) {
            plugin.getMessages().sendPrefixedMessage(player, "error.item_not_found");
            return false;
        }

        // Check if player has enough money
        if (!plugin.getEconomyManager().hasEnough(player, item.getPrice())) {
            plugin.getMessages().sendPrefixedMessage(player, "error.not_enough_money",
                    "price", plugin.getEconomyManager().format(item.getPrice()),
                    "balance", plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player)));
            return false;
        }

        // Process purchase based on type
        boolean success = false;
        UUID playerUuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        switch (item.getType()) {
            case TIMED_PERMISSION:
                success = processTimedPermissionPurchase(player, item, playerUuid, now);
                break;
            case LIMITED_COMMAND:
                success = processLimitedCommandPurchase(player, item, playerUuid, now);
                break;
            case PERMANENT_PERMISSION:
                success = processPermanentPermissionPurchase(player, item, playerUuid, now);
                break;
            case ONE_TIME_COMMAND:
                success = processOneTimeCommandPurchase(player, item, playerUuid, now);
                break;
            case HOME_SLOT:
                success = processHomeSlotPurchase(player, item, playerUuid, now);
                break;
            default:
                plugin.getMessages().sendPrefixedMessage(player, "error.invalid_purchase_type");
                return false;
        }

        if (success) {
            // Charge the player
            plugin.getEconomyManager().withdraw(player, item.getPrice());

            // Send success message
            plugin.getMessages().sendPrefixedMessage(player, "purchase.success",
                    "item", item.getName(),
                    "price", plugin.getEconomyManager().format(item.getPrice()));

            return true;
        }

        return false;
    }

    /**
     * Processes a timed permission purchase
     */
    private boolean processTimedPermissionPurchase(Player player, ShopItem item, UUID playerUuid, long now) {
        if (item.getDuration() <= 0) {
            plugin.getMessages().sendPrefixedMessage(player, "error.invalid_duration");
            return false;
        }

        // Calculate expiration time
        long expirationTime = now + (item.getDuration() * 1000L);

        // Try to grant the permission first using LuckPerms
        boolean permissionGranted = plugin.getPermissionManager().grantPermission(
                player,
                item.getPermission(),
                expirationTime
        );

        if (!permissionGranted) {
            plugin.getLogger().log(Level.WARNING, "Failed to grant permission " + item.getPermission() + " to " + player.getName());
            plugin.getMessages().sendPrefixedMessage(player, "error.permission_grant_failed");
            return false;
        }

        // Create purchase
        Purchase purchase = new Purchase(
                playerUuid,
                item.getId(),
                now,
                expirationTime,
                -1, // Unlimited uses
                true // Active
        );

        // Save purchase
        int purchaseId = plugin.getStorageManager().savePurchase(purchase);
        if (purchaseId == -1) {
            plugin.getMessages().sendPrefixedMessage(player, "error.database_error");
            // Try to revoke the permission since we failed to save
            plugin.getPermissionManager().revokePermission(player, item.getPermission());
            return false;
        }

        // Create timed permission
        TimedPermission timedPermission = new TimedPermission(
                playerUuid,
                item.getPermission(),
                expirationTime,
                purchaseId
        );

        // Add timed permission to our tracking
        plugin.getPermissionManager().addTimedPermission(timedPermission);

        // Send additional message about duration
        plugin.getMessages().sendPrefixedMessage(player, "purchase.permission_duration",
                "duration", formatTimeRemaining(expirationTime - now));

        return true;
    }

    /**
     * Processes a limited command purchase
     */
    private boolean processLimitedCommandPurchase(Player player, ShopItem item, UUID playerUuid, long now) {
        if (item.getUses() <= 0) {
            plugin.getMessages().sendPrefixedMessage(player, "error.invalid_uses");
            return false;
        }

        // Create purchase
        Purchase purchase = new Purchase(
                playerUuid,
                item.getId(),
                now,
                0, // No expiration
                item.getUses(),
                true // Active
        );

        // Save purchase
        int purchaseId = plugin.getStorageManager().savePurchase(purchase);
        if (purchaseId == -1) {
            plugin.getMessages().sendPrefixedMessage(player, "error.database_error");
            return false;
        }

        // Update purchase with ID
        purchase = new Purchase(
                purchaseId,
                playerUuid,
                item.getId(),
                now,
                0,
                item.getUses(),
                true
        );

        // Add to usage manager
        plugin.getUsageManager().addPurchase(purchase);

        // Send additional message about uses
        plugin.getMessages().sendPrefixedMessage(player, "purchase.command_usage",
                "uses", String.valueOf(item.getUses()));

        return true;
    }

    /**
     * Processes a permanent permission purchase
     */
    private boolean processPermanentPermissionPurchase(Player player, ShopItem item, UUID playerUuid, long now) {
        // Grant permission with LuckPerms (0 expiration means permanent)
        boolean permissionGranted = plugin.getPermissionManager().grantPermission(
                player,
                item.getPermission(),
                0
        );

        if (!permissionGranted) {
            plugin.getLogger().log(Level.WARNING, "Failed to grant permanent permission " + item.getPermission() + " to " + player.getName());
            plugin.getMessages().sendPrefixedMessage(player, "error.permission_grant_failed");
            return false;
        }

        // Create purchase
        Purchase purchase = new Purchase(
                playerUuid,
                item.getId(),
                now,
                0, // No expiration
                -1, // Unlimited uses
                true // Active
        );

        // Save purchase
        int purchaseId = plugin.getStorageManager().savePurchase(purchase);
        if (purchaseId == -1) {
            plugin.getMessages().sendPrefixedMessage(player, "error.database_error");
            // Try to revoke the permission since we failed to save
            plugin.getPermissionManager().revokePermission(player, item.getPermission());
            return false;
        }

        // Send additional message
        plugin.getMessages().sendPrefixedMessage(player, "purchase.permission_permanent");

        return true;
    }

    /**
     * Processes a one-time command purchase
     */
    private boolean processOneTimeCommandPurchase(Player player, ShopItem item, UUID playerUuid, long now) {
        // Execute command
        String command = item.getCommand().replace("{player}", player.getName());
        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        if (!success) {
            plugin.getMessages().sendPrefixedMessage(player, "error.command_failed");
            return false;
        }

        // Create purchase (for record keeping)
        Purchase purchase = new Purchase(
                playerUuid,
                item.getId(),
                now,
                now, // Expired immediately
                0, // No uses
                false // Inactive
        );

        // Save purchase
        plugin.getStorageManager().savePurchase(purchase);

        return true;
    }

    /**
     * Processes a home slot purchase
     */
    private boolean processHomeSlotPurchase(Player player, ShopItem item, UUID playerUuid, long now) {
        // We need to check if the server is using EssentialsX or CMI
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            // EssentialsX
            String command = "sethome-limit " + player.getName() + " " + item.getUses();
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            if (!success) {
                plugin.getMessages().sendPrefixedMessage(player, "error.command_failed");
                return false;
            }
        } else if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            // CMI
            String command = "cmi sethome add " + player.getName() + " " + item.getUses();
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            if (!success) {
                plugin.getMessages().sendPrefixedMessage(player, "error.command_failed");
                return false;
            }
        } else {
            plugin.getMessages().sendPrefixedMessage(player, "error.no_home_plugin");
            return false;
        }

        // Create purchase (for record keeping)
        Purchase purchase = new Purchase(
                playerUuid,
                item.getId(),
                now,
                now, // Expired immediately
                0, // No uses
                false // Inactive
        );

        // Save purchase
        plugin.getStorageManager().savePurchase(purchase);

        return true;
    }

    /**
     * Loads a player's purchases
     *
     * @param playerUuid Player UUID
     */
    public void loadPlayerPurchases(UUID playerUuid) {
        List<Purchase> purchases = plugin.getStorageManager().getPlayerPurchases(playerUuid);

        for (Purchase purchase : purchases) {
            if (!purchase.isActive()) {
                continue;
            }

            ShopItem item = plugin.getConfigManager().getShopItem(purchase.getItemId());
            if (item == null) {
                continue;
            }

            // Process based on purchase type
            switch (item.getType()) {
                case TIMED_PERMISSION:
                    if (!purchase.hasExpired()) {
                        TimedPermission timedPermission = new TimedPermission(
                                playerUuid,
                                item.getPermission(),
                                purchase.getExpirationTime(),
                                purchase.getId()
                        );
                        plugin.getPermissionManager().addTimedPermission(timedPermission);
                    } else {
                        // Deactivate expired purchase
                        deactivatePurchase(purchase.getId());
                    }
                    break;
                case LIMITED_COMMAND:
                    if (purchase.hasUsesRemaining()) {
                        plugin.getUsageManager().addPurchase(purchase);
                    } else {
                        // Deactivate purchase with no uses left
                        deactivatePurchase(purchase.getId());
                    }
                    break;
                case PERMANENT_PERMISSION:
                    // For online players, grant the permission
                    Player player = Bukkit.getPlayer(playerUuid);
                    if (player != null && player.isOnline()) {
                        plugin.getPermissionManager().grantPermission(player, item.getPermission(), 0);
                    }
                    break;
            }
        }
    }

    /**
     * Deactivates a purchase
     *
     * @param purchaseId Purchase ID
     */
    public void deactivatePurchase(int purchaseId) {
        plugin.getStorageManager().deactivatePurchase(purchaseId);
    }

    /**
     * Updates a purchase's remaining uses
     *
     * @param purchaseId Purchase ID
     * @param remainingUses Remaining uses
     */
    public void updatePurchaseUses(int purchaseId, int remainingUses) {
        plugin.getStorageManager().updatePurchaseUses(purchaseId, remainingUses);
    }

    /**
     * Formats time remaining
     *
     * @param millis Time in milliseconds
     * @return Formatted time
     */
    private String formatTimeRemaining(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }

        if (hours % 24 > 0) {
            sb.append(hours % 24).append(" hour").append(hours % 24 > 1 ? "s" : "").append(" ");
        }

        if (minutes % 60 > 0) {
            sb.append(minutes % 60).append(" minute").append(minutes % 60 > 1 ? "s" : "").append(" ");
        }

        if (seconds % 60 > 0 || sb.length() == 0) {
            sb.append(seconds % 60).append(" second").append(seconds % 60 != 1 ? "s" : "");
        } else {
            // Remove trailing space
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}