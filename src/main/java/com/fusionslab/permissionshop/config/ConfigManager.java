package com.fusionslab.permissionshop.config;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.PurchaseType;
import com.fusionslab.permissionshop.models.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages plugin configuration
 */
public class ConfigManager {

    private final PermissionsShop plugin;
    private FileConfiguration config;
    private FileConfiguration shopConfig;
    private final Map<String, ShopItem> shopItems = new HashMap<>();

    public ConfigManager(PermissionsShop plugin) {
        this.plugin = plugin;
        loadConfig();
        loadShopConfig();
    }

    /**
     * Loads the main configuration file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Loads the shop configuration file
     */
    public void loadShopConfig() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");

        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        loadShopItems();
    }

    /**
     * Reloads all configuration files
     */
    public void reloadConfigs() {
        loadConfig();
        loadShopConfig();
    }

    /**
     * Loads shop items from shop.yml
     */
    private void loadShopItems() {
        shopItems.clear();

        ConfigurationSection itemsSection = shopConfig.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("No items found in shop.yml!");
            return;
        }

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) continue;

            String name = itemSection.getString("name", itemId);
            String description = itemSection.getString("description", "");
            double price = itemSection.getDouble("price", 0);
            String type = itemSection.getString("type", "").toUpperCase();
            PurchaseType purchaseType;

            try {
                purchaseType = PurchaseType.valueOf(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid purchase type for item " + itemId + ": " + type);
                continue;
            }

            int duration = itemSection.getInt("duration", 0);
            int uses = itemSection.getInt("uses", 0);
            String permission = itemSection.getString("permission", "");
            String command = itemSection.getString("command", "");
            boolean displayInShop = itemSection.getBoolean("display_in_shop", true);

            ShopItem shopItem = new ShopItem(
                    itemId,
                    name,
                    description,
                    price,
                    purchaseType,
                    duration,
                    uses,
                    permission,
                    command,
                    displayInShop
            );

            shopItems.put(itemId, shopItem);
        }

        plugin.getLogger().info("Loaded " + shopItems.size() + " shop items.");
    }

    /**
     * Gets a shop item by its ID
     *
     * @param id Item ID
     * @return ShopItem or null if not found
     */
    public ShopItem getShopItem(String id) {
        return shopItems.get(id);
    }

    /**
     * Gets all shop items
     *
     * @return Map of shop items
     */
    public Map<String, ShopItem> getShopItems() {
        return new HashMap<>(shopItems);
    }

    /**
     * Gets all shop items that should be displayed in the shop
     *
     * @return List of displayable shop items
     */
    public List<ShopItem> getDisplayableShopItems() {
        List<ShopItem> items = new ArrayList<>();
        for (ShopItem item : shopItems.values()) {
            if (item.isDisplayInShop()) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Gets the main configuration
     *
     * @return FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the shop configuration
     *
     * @return FileConfiguration
     */
    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    /**
     * Saves the shop configuration
     */
    public void saveShopConfig() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save shop config to " + shopFile, e);
        }
    }
}