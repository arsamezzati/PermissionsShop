package com.fusionslab.permissionshop.managers;

import com.fusionslab.permissionshop.PermissionsShop;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manages timers for timed permissions
 */
public class TimerManager {

    private final PermissionsShop plugin;
    private BukkitTask timerTask;

    public TimerManager(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the timer task
     */
    public void startTimerTask() {
        // Cancel existing task if running
        cancelTimerTask();

        // Check interval from config (default: 30 seconds)
        int checkInterval = plugin.getConfig().getInt("check_interval", 30) * 20;

        // Start new task
        timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Check for expired permissions
            plugin.getPermissionManager().cleanupExpiredPermissions();
        }, checkInterval, checkInterval);
    }

    /**
     * Cancels the timer task
     */
    public void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}