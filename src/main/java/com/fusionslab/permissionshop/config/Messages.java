package com.fusionslab.permissionshop.config;

import com.fusionslab.permissionshop.PermissionsShop;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages plugin messages
 */
public class Messages {

    private final PermissionsShop plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages = new HashMap<>();
    private final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public Messages(PermissionsShop plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads messages from messages.yml
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messages.clear();

        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messages.put(key, messagesConfig.getString(key));
            }
        }
    }

    /**
     * Gets a message by its key and replaces placeholders
     *
     * @param key Message key
     * @param replacements Key-value pairs for placeholders
     * @return Formatted message
     */
    public String getMessage(String key, Object... replacements) {
        String message = messages.getOrDefault(key, "Missing message: " + key);

        // Apply color codes
        message = translateHexColorCodes(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Replace placeholders
        if (replacements != null && replacements.length > 0) {
            if (replacements.length % 2 != 0) {
                plugin.getLogger().warning("Uneven number of replacement arguments for message: " + key);
            } else {
                for (int i = 0; i < replacements.length; i += 2) {
                    if (replacements[i] != null && replacements[i + 1] != null) {
                        message = message.replace("{" + replacements[i].toString() + "}", replacements[i + 1].toString());
                    }
                }
            }
        }

        return message;
    }

    /**
     * Sends a message to a player
     *
     * @param player Player to send message to
     * @param key Message key
     * @param replacements Key-value pairs for placeholders
     */
    public void sendMessage(Player player, String key, Object... replacements) {
        String message = getMessage(key, replacements);
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    /**
     * Sends the plugin prefix followed by a message to a player
     *
     * @param player Player to send message to
     * @param key Message key
     * @param replacements Key-value pairs for placeholders
     */
    public void sendPrefixedMessage(Player player, String key, Object... replacements) {
        String prefix = getMessage("prefix");
        String message = getMessage(key, replacements);
        if (!message.isEmpty()) {
            player.sendMessage(prefix + " " + message);
        }
    }

    /**
     * Translates hex color codes in a string
     *
     * @param message Message with potential hex codes
     * @return Message with translated hex codes
     */
    private String translateHexColorCodes(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group();
            matcher.appendReplacement(buffer, ChatColor.valueOf(hex).toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}