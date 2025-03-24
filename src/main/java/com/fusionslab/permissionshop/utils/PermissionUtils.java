package com.fusionslab.permissionshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Utility methods for permission operations
 */
public class PermissionUtils {

    /**
     * Checks if a player has a permission
     *
     * @param player Player to check
     * @param permission Permission to check
     * @return Whether the player has the permission
     */
    public static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    /**
     * Checks if a permission exists for any plugin
     *
     * @param permission Permission to check
     * @return Whether the permission exists
     */
    public static boolean permissionExists(String permission) {
        // Check if the permission is in the bukkit.yml
        if (Bukkit.getPluginManager().getPermission(permission) != null) {
            return true;
        }

        // Check if the permission is a default Bukkit permission
        if (permission.startsWith("bukkit.") ||
                permission.startsWith("minecraft.") ||
                permission.startsWith("craftbukkit.")) {
            return true;
        }

        // Check if the permission belongs to any plugin
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String pluginName = plugin.getName().toLowerCase();
            if (permission.startsWith(pluginName + ".")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the plugin that a permission belongs to
     *
     * @param permission Permission to check
     * @return Plugin name or "Unknown"
     */
    public static String getPermissionPlugin(String permission) {
        // Check if the permission is a default Bukkit permission
        if (permission.startsWith("bukkit.") ||
                permission.startsWith("minecraft.")) {
            return "Bukkit";
        }

        if (permission.startsWith("craftbukkit.")) {
            return "CraftBukkit";
        }

        // Check if the permission belongs to any plugin
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String pluginName = plugin.getName().toLowerCase();
            if (permission.startsWith(pluginName + ".")) {
                return plugin.getName();
            }
        }

        return "Unknown";
    }
}