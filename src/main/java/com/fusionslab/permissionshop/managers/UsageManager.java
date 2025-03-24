package com.fusionslab.permissionshop.managers;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.Purchase;
import com.fusionslab.permissionshop.models.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages limited-use commands
 */
public class UsageManager implements Listener {

    private final PermissionsShop plugin;
    private final Map<UUID, Map<String, Purchase>> playerPurchases;

    public UsageManager(PermissionsShop plugin) {
        this.plugin = plugin;
        this.playerPurchases = new ConcurrentHashMap<>();

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a purchase to the usage manager
     *
     * @param purchase Purchase to add
     */
    public void addPurchase(Purchase purchase) {
        UUID playerUuid = purchase.getPlayerUuid();

        playerPurchases.putIfAbsent(playerUuid, new HashMap<>());
        Map<String, Purchase> purchases = playerPurchases.get(playerUuid);

        // Add the purchase
        purchases.put(purchase.getItemId(), purchase);
    }

    /**
     * Removes a purchase from the usage manager
     *
     * @param playerUuid Player UUID
     * @param itemId Item ID
     */
    public void removePurchase(UUID playerUuid, String itemId) {
        if (playerPurchases.containsKey(playerUuid)) {
            Map<String, Purchase> purchases = playerPurchases.get(playerUuid);
            purchases.remove(itemId);
        }
    }

    /**
     * Gets a player's purchases
     *
     * @param playerUuid Player UUID
     * @return Map of purchases
     */
    public Map<String, Purchase> getPlayerPurchases(UUID playerUuid) {
        return playerPurchases.getOrDefault(playerUuid, new HashMap<>());
    }

    /**
     * Checks if a player has a purchase
     *
     * @param playerUuid Player UUID
     * @param itemId Item ID
     * @return Whether the player has the purchase
     */
    public boolean hasPurchase(UUID playerUuid, String itemId) {
        if (playerPurchases.containsKey(playerUuid)) {
            return playerPurchases.get(playerUuid).containsKey(itemId);
        }
        return false;
    }

    /**
     * Gets a purchase
     *
     * @param playerUuid Player UUID
     * @param itemId Item ID
     * @return Purchase or null if not found
     */
    public Purchase getPurchase(UUID playerUuid, String itemId) {
        if (playerPurchases.containsKey(playerUuid)) {
            return playerPurchases.get(playerUuid).get(itemId);
        }
        return null;
    }

    /**
     * Checks if a player can use a command
     *
     * @param player Player to check
     * @param command Command to check
     * @return Whether the player can use the command
     */
    public boolean canUseCommand(Player player, String command) {
        UUID playerUuid = player.getUniqueId();

        if (!playerPurchases.containsKey(playerUuid)) {
            return false;
        }

        for (Purchase purchase : playerPurchases.get(playerUuid).values()) {
            ShopItem item = plugin.getConfigManager().getShopItem(purchase.getItemId());

            if (item == null) {
                continue;
            }

            // Check if the command matches and the purchase has uses remaining
            if (command.startsWith(item.getCommand()) && purchase.hasUsesRemaining()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Decrements the usage for a command
     *
     * @param player Player using the command
     * @param command Command being used
     * @return Whether the usage was decremented
     */
    public boolean decrementUsage(Player player, String command) {
        UUID playerUuid = player.getUniqueId();

        if (!playerPurchases.containsKey(playerUuid)) {
            return false;
        }

        for (Purchase purchase : playerPurchases.get(playerUuid).values()) {
            ShopItem item = plugin.getConfigManager().getShopItem(purchase.getItemId());

            if (item == null) {
                continue;
            }

            // Check if the command matches and the purchase has uses remaining
            if (command.startsWith(item.getCommand()) && purchase.hasUsesRemaining()) {
                // Decrement uses
                int remainingUses = purchase.decrementUses();

                // Update purchase in database
                plugin.getPurchaseManager().updatePurchaseUses(purchase.getId(), remainingUses);

                // If no uses left, deactivate purchase
                if (remainingUses == 0) {
                    purchase.setActive(false);
                    plugin.getPurchaseManager().deactivatePurchase(purchase.getId());
                    removePurchase(playerUuid, purchase.getItemId());
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Handles command usage
     *
     * @param event PlayerCommandPreprocessEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        // Check if the command is limited-use
        for (ShopItem item : plugin.getConfigManager().getShopItems().values()) {
            if (item.getCommand().toLowerCase().equals(command)) {
                // Check if player has permission to use the command
                if (player.hasPermission(item.getPermission())) {
                    // Player has permission, allow command
                    return;
                }

                // Check if player has purchased limited uses
                if (canUseCommand(player, command)) {
                    // Decrement usage
                    decrementUsage(player, command);

                    // Allow command
                    return;
                }

                // Player doesn't have permission or purchased uses, show message
                plugin.getMessages().sendPrefixedMessage(player, "error.no_permission_or_uses",
                        "command", command);

                // Cancel the command
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Cleans up a player's purchases when they quit
     *
     * @param playerUuid Player UUID
     */
    public void cleanupPlayerPurchases(UUID playerUuid) {
        playerPurchases.remove(playerUuid);
    }
}