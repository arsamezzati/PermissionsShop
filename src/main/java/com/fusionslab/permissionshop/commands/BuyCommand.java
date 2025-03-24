package com.fusionslab.permissionshop.commands;

import com.fusionslab.permissionshop.PermissionsShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for buying permissions and commands
 */
public class BuyCommand implements CommandExecutor {

    private final PermissionsShop plugin;

    public BuyCommand(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("permissionshop.buy")) {
            plugin.getMessages().sendPrefixedMessage(player, "error.no_permission");
            return true;
        }

        // Check args
        if (args.length < 1) {
            plugin.getMessages().sendPrefixedMessage(player, "buy.usage");
            return true;
        }

        // Get item ID
        String itemId = args[0];

        // Try to make purchase
        boolean success = plugin.getPurchaseManager().makePurchase(player, itemId);

        if (!success) {
            // Error message already sent in makePurchase
            return true;
        }

        return true;
    }
}