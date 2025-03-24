package com.fusionslab.permissionshop.commands;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.Purchase;
import com.fusionslab.permissionshop.models.TimedPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Admin command for managing the permissions shop
 */
public class PermShopAdminCommand implements CommandExecutor {

    private final PermissionsShop plugin;

    public PermShopAdminCommand(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("permissionshop.admin")) {
            if (sender instanceof Player) {
                plugin.getMessages().sendPrefixedMessage((Player) sender, "error.no_permission");
            } else {
                sender.sendMessage("You don't have permission to use this command.");
            }
            return true;
        }

        // Check args
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        // Handle subcommands
        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                reloadCommand(sender);
                break;
            case "give":
                giveCommand(sender, args);
                break;
            case "revoke":
                revokeCommand(sender, args);
                break;
            case "list":
                listCommand(sender, args);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    /**
     * Sends command usage to sender
     *
     * @param sender Command sender
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§c§lPermissionShop Admin Commands:");
        sender.sendMessage("§c/psadmin reload §7- Reload configuration");
        sender.sendMessage("§c/psadmin give <player> <itemId> §7- Give a permission or command to a player");
        sender.sendMessage("§c/psadmin revoke <player> <itemId> §7- Revoke a permission or command from a player");
        sender.sendMessage("§c/psadmin list <player> §7- List a player's purchases");
    }

    /**
     * Handles the reload subcommand
     *
     * @param sender Command sender
     */
    private void reloadCommand(CommandSender sender) {
        // Reload configs
        plugin.getConfigManager().reloadConfigs();
        plugin.getMessages().loadMessages();

        // Send success message
        if (sender instanceof Player) {
            plugin.getMessages().sendPrefixedMessage((Player) sender, "admin.reload_success");
        } else {
            sender.sendMessage("Configurations reloaded successfully.");
        }
    }

    /**
     * Handles the give subcommand
     *
     * @param sender Command sender
     * @param args Command arguments
     */
    private void giveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /psadmin give <player> <itemId>");
            return;
        }

        String playerName = args[1];
        String itemId = args[2];

        // Get player
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return;
        }

        // Get item
        if (plugin.getConfigManager().getShopItem(itemId) == null) {
            sender.sendMessage("§cItem not found: " + itemId);
            return;
        }

        // Make purchase for free
        boolean success = plugin.getPurchaseManager().makePurchase(target, itemId);

        if (success) {
            sender.sendMessage("§aGave " + itemId + " to " + target.getName() + " successfully.");
        } else {
            sender.sendMessage("§cFailed to give " + itemId + " to " + target.getName() + ".");
        }
    }

    /**
     * Handles the revoke subcommand
     *
     * @param sender Command sender
     * @param args Command arguments
     */
    private void revokeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /psadmin revoke <player> <itemId>");
            return;
        }

        String playerName = args[1];
        String itemId = args[2];

        // Get player
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return;
        }

        // Get item
        if (plugin.getConfigManager().getShopItem(itemId) == null) {
            sender.sendMessage("§cItem not found: " + itemId);
            return;
        }

        UUID playerUuid = target.getUniqueId();

        // Find the purchase
        List<Purchase> purchases = plugin.getStorageManager().getPlayerPurchases(playerUuid);
        boolean found = false;

        for (Purchase purchase : purchases) {
            if (purchase.getItemId().equals(itemId) && purchase.isActive()) {
                // Deactivate purchase
                plugin.getStorageManager().deactivatePurchase(purchase.getId());

                // Revoke permission if applicable
                if (plugin.getConfigManager().getShopItem(itemId).getPermission() != null) {
                    plugin.getPermissionManager().revokePermission(target, plugin.getConfigManager().getShopItem(itemId).getPermission());
                }

                found = true;
                break;
            }
        }

        if (found) {
            sender.sendMessage("§aRevoked " + itemId + " from " + target.getName() + " successfully.");
        } else {
            sender.sendMessage("§cPlayer does not have an active purchase of " + itemId + ".");
        }
    }

    /**
     * Handles the list subcommand
     *
     * @param sender Command sender
     * @param args Command arguments
     */
    private void listCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /psadmin list <player>");
            return;
        }

        String playerName = args[1];

        // Get player
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return;
        }

        UUID playerUuid = target.getUniqueId();

        // Get purchases
        List<Purchase> purchases = plugin.getStorageManager().getPlayerPurchases(playerUuid);

        // List purchases
        sender.sendMessage("§a§lPurchases for " + target.getName() + ":");

        if (purchases.isEmpty()) {
            sender.sendMessage("§7No purchases found.");
            return;
        }

        for (Purchase purchase : purchases) {
            String itemId = purchase.getItemId();
            String status = purchase.isActive() ? "§aActive" : "§cInactive";
            String expiresIn = purchase.getExpirationTime() > 0 ?
                    "§7Expires in: §f" + formatTimeRemaining(purchase.getExpirationTime()) :
                    "§7No expiration";
            String uses = purchase.getRemainingUses() != -1 ?
                    "§7Uses: §f" + purchase.getRemainingUses() :
                    "§7Unlimited uses";

            sender.sendMessage("§f- " + itemId + " §7[" + status + "§7] " + expiresIn + ", " + uses);
        }

        // List timed permissions
        List<TimedPermission> timedPermissions = plugin.getPermissionManager().getTimedPermissions(playerUuid);

        if (!timedPermissions.isEmpty()) {
            sender.sendMessage("§a§lTimed Permissions:");

            for (TimedPermission timedPermission : timedPermissions) {
                String permission = timedPermission.getPermission();
                String timeRemaining = formatTimeRemaining(timedPermission.getExpirationTime());

                sender.sendMessage("§f- " + permission + " §7(Expires in: §f" + timeRemaining + "§7)");
            }
        }
    }

    /**
     * Formats time remaining
     *
     * @param expirationTime Expiration timestamp
     * @return Formatted time remaining
     */
    private String formatTimeRemaining(long expirationTime) {
        long now = System.currentTimeMillis();
        long remaining = expirationTime - now;

        if (remaining <= 0) {
            return "Expired";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }

        sb.append(seconds).append("s");

        return sb.toString();
    }
}