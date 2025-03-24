package com.fusionslab.permissionshop.listeners;

import com.fusionslab.permissionshop.PermissionsShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player quit events
 */
public class PlayerQuitListener implements Listener {

    private final PermissionsShop plugin;

    public PlayerQuitListener(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Clean up player permissions
        plugin.getPermissionManager().cleanupPlayerPermissions(player.getUniqueId());

        // Clean up player purchases
        plugin.getUsageManager().cleanupPlayerPurchases(player.getUniqueId());
    }
}