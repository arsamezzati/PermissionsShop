package com.fusionslab.permissionshop;

import com.fusionslab.permissionshop.commands.BuyCommand;
import com.fusionslab.permissionshop.commands.PermShopAdminCommand;
import com.fusionslab.permissionshop.commands.ShopCommand;
import com.fusionslab.permissionshop.config.ConfigManager;
import com.fusionslab.permissionshop.config.Messages;
import com.fusionslab.permissionshop.economy.EconomyManager;
import com.fusionslab.permissionshop.listeners.PlayerJoinListener;
import com.fusionslab.permissionshop.listeners.PlayerQuitListener;
import com.fusionslab.permissionshop.managers.PermissionManager;
import com.fusionslab.permissionshop.managers.PurchaseManager;
import com.fusionslab.permissionshop.managers.TimerManager;
import com.fusionslab.permissionshop.managers.UsageManager;
import com.fusionslab.permissionshop.storage.SQLiteStorage;
import com.fusionslab.permissionshop.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the PermissionsShop plugin
 */
public class PermissionsShop extends JavaPlugin {

    private static PermissionsShop instance;
    private ConfigManager configManager;
    private Messages messages;
    private EconomyManager economyManager;
    private PermissionManager permissionManager;
    private PurchaseManager purchaseManager;
    private TimerManager timerManager;
    private UsageManager usageManager;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);

        // Setup economy
        this.economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getLogger().severe("No economy plugin found! Disabling PermissionsShop...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup storage
        String storageType = getConfig().getString("storage.type", "sqlite").toLowerCase();
        if (storageType.equals("mysql")) {
            // MySQL implementation would go here
            this.storageManager = new SQLiteStorage(this); // Fallback to SQLite for now
        } else {
            this.storageManager = new SQLiteStorage(this);
        }

        // Initialize managers
        this.permissionManager = new PermissionManager(this);
        this.purchaseManager = new PurchaseManager(this);
        this.timerManager = new TimerManager(this);
        this.usageManager = new UsageManager(this);

        // Register commands
        getCommand("permshop").setExecutor(new ShopCommand(this));
        getCommand("psbuy").setExecutor(new BuyCommand(this));
        getCommand("psadmin").setExecutor(new PermShopAdminCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        // Start tasks
        this.timerManager.startTimerTask();

        getLogger().info("PermissionsShop has been enabled!");
    }

    @Override
    public void onDisable() {
        if (timerManager != null) {
            timerManager.cancelTimerTask();
        }

        if (storageManager != null) {
            storageManager.closeConnection();
        }

        getLogger().info("PermissionsShop has been disabled!");
    }

    /**
     * Get the plugin instance
     * @return PermissionsShop instance
     */
    public static PermissionsShop getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public PurchaseManager getPurchaseManager() {
        return purchaseManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public UsageManager getUsageManager() {
        return usageManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}