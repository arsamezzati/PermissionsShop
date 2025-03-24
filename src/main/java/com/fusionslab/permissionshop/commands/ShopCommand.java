package com.fusionslab.permissionshop.commands;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.ShopItem;
import com.fusionslab.permissionshop.utils.TimeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command for viewing the permissions shop
 */
public class ShopCommand implements CommandExecutor {

    private final PermissionsShop plugin;

    public ShopCommand(PermissionsShop plugin) {
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
        if (!player.hasPermission("permissionshop.shop")) {
            plugin.getMessages().sendPrefixedMessage(player, "error.no_permission");
            return true;
        }

        // Show shop
        showShop(player);

        return true;
    }

    /**
     * Shows the permissions shop to a player
     *
     * @param player Player to show shop to
     */
    private void showShop(Player player) {
        List<ShopItem> items = plugin.getConfigManager().getDisplayableShopItems();

        if (items.isEmpty()) {
            plugin.getMessages().sendPrefixedMessage(player, "shop.empty");
            return;
        }

        // Send header
        plugin.getMessages().sendMessage(player, "shop.header");

        // Send items
        for (ShopItem item : items) {
            String duration = "";
            String uses = "";

            switch (item.getType()) {
                case TIMED_PERMISSION:
                    duration = TimeUtils.formatTime(item.getDuration());
                    break;
                case LIMITED_COMMAND:
                    uses = String.valueOf(item.getUses());
                    break;
                case PERMANENT_PERMISSION:
                    duration = "Permanent";
                    break;
                case ONE_TIME_COMMAND:
                    uses = "1";
                    break;
                case HOME_SLOT:
                    uses = "+" + item.getUses() + " slots";
                    break;
            }

            plugin.getMessages().sendMessage(player, "shop.item",
                    "id", item.getId(),
                    "name", item.getName(),
                    "description", item.getDescription(),
                    "price", plugin.getEconomyManager().format(item.getPrice()),
                    "duration", duration,
                    "uses", uses,
                    "type", item.getType().toString()
            );
        }

        // Send footer
        plugin.getMessages().sendMessage(player, "shop.footer");
    }
}