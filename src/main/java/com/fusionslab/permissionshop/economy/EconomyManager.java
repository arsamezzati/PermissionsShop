package com.fusionslab.permissionshop.economy;

import com.fusionslab.permissionshop.PermissionsShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages economy operations using Vault
 */
public class EconomyManager {

    private final PermissionsShop plugin;
    private Economy economy;

    public EconomyManager(PermissionsShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets up the economy hook with Vault
     *
     * @return Whether setup was successful
     */
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Checks if a player has enough money
     *
     * @param player Player to check
     * @param amount Amount to check
     * @return Whether the player has enough money
     */
    public boolean hasEnough(Player player, double amount) {
        return economy.has(player, amount);
    }

    /**
     * Withdraws money from a player
     *
     * @param player Player to withdraw from
     * @param amount Amount to withdraw
     * @return Whether the withdrawal was successful
     */
    public boolean withdraw(Player player, double amount) {
        if (!hasEnough(player, amount)) {
            return false;
        }

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Deposits money to a player
     *
     * @param player Player to deposit to
     * @param amount Amount to deposit
     * @return Whether the deposit was successful
     */
    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Gets a player's balance
     *
     * @param player Player to get balance of
     * @return Player's balance
     */
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    /**
     * Gets a player's balance
     *
     * @param player Offline player to get balance of
     * @return Player's balance
     */
    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    /**
     * Formats an amount according to the economy's format
     *
     * @param amount Amount to format
     * @return Formatted amount
     */
    public String format(double amount) {
        return economy.format(amount);
    }

    /**
     * Gets the economy instance
     *
     * @return Economy instance
     */
    public Economy getEconomy() {
        return economy;
    }
}