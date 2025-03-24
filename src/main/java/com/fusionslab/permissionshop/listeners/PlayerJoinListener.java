package com.fusionslab.permissionshop.listeners;

import com.fusionslab.permissionshop.PermissionsShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles player join events
 */
public class PlayerJoinListener implements Listener {

    private final PermissionsShop plugin;

    public PlayerJoinListener(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Schedule delayed task to load player data after all plugins have loaded
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            // Load player's permissions
            plugin.getPermissionManager().loadPlayerPermissions(player);

            // Load player's purchases
            plugin.getPurchaseManager().loadPlayerPurchases(player.getUniqueId());
        }, 20L); // 1 second delay
    }
}