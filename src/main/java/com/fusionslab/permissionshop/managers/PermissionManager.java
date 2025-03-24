package com.fusionslab.permissionshop.managers;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.TimedPermission;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages player permissions using LuckPerms
 */
public class PermissionManager {

    private final PermissionsShop plugin;
    private final Map<UUID, List<TimedPermission>> timedPermissions;
    private final Map<UUID, Map<String, PermissionAttachment>> playerPermissions;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled = false;

    public PermissionManager(PermissionsShop plugin) {
        this.plugin = plugin;
        this.timedPermissions = new HashMap<>();
        this.playerPermissions = new HashMap<>();

        // Try to get LuckPerms API
        try {
            this.luckPerms = LuckPermsProvider.get();
            luckPermsEnabled = true;
            plugin.getLogger().info("Successfully hooked into LuckPerms!");
        } catch (IllegalStateException e) {
            plugin.getLogger().severe("LuckPerms not found! Falling back to Bukkit permission system.");
        }
    }

    /**
     * Grants a permission to a player
     *
     * @param player Player to grant permission to
     * @param permission Permission to grant
     * @param expiryTimeMillis Expiration time in milliseconds (0 for permanent)
     * @return Whether the permission was granted successfully
     */
    public boolean grantPermission(Player player, String permission, long expiryTimeMillis) {
        if (luckPermsEnabled) {
            return grantLuckPermsPermission(player, permission, expiryTimeMillis);
        } else {
            // Fallback to Bukkit permissions
            UUID playerUuid = player.getUniqueId();

            playerPermissions.putIfAbsent(playerUuid, new HashMap<>());
            Map<String, PermissionAttachment> playerPerms = playerPermissions.get(playerUuid);

            // Remove existing permission if present
            if (playerPerms.containsKey(permission)) {
                player.removeAttachment(playerPerms.get(permission));
                playerPerms.remove(permission);
            }

            // Add new permission
            PermissionAttachment attachment = player.addAttachment(plugin);
            attachment.setPermission(permission, true);
            playerPerms.put(permission, attachment);

            player.updateCommands();
            return true;
        }
    }

    /**
     * Grants a permission using LuckPerms
     *
     * @param player Player to grant permission to
     * @param permission Permission to grant
     * @param expiryTimeMillis Expiration time in milliseconds (0 for permanent)
     * @return Whether the permission was granted successfully
     */
    private boolean grantLuckPermsPermission(Player player, String permission, long expiryTimeMillis) {
        try {
            // Use modifyUser for atomic load-modify-save operations as recommended in docs
            luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                // Build the permission node using the specific PermissionNode builder
                PermissionNode.Builder nodeBuilder = PermissionNode.builder(permission);

                // Add expiry if not permanent
                if (expiryTimeMillis > 0) {
                    Duration duration = Duration.ofMillis(expiryTimeMillis - System.currentTimeMillis());
                    nodeBuilder.expiry(duration);
                }

                // Create the node
                PermissionNode node = nodeBuilder.build();

                // Add the node
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    plugin.getLogger().info("Granted permission " + permission + " to " + player.getName() +
                            (expiryTimeMillis > 0 ? " until " + new Date(expiryTimeMillis) : " permanently"));
                } else {
                    plugin.getLogger().warning("Failed to grant permission " + permission + " to " + player.getName() +
                            ": " + result.name());
                }
            });

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error granting LuckPerms permission to " + player.getName(), e);
            return false;
        }
    }

    /**
     * Revokes a permission from a player
     *
     * @param player Player to revoke permission from
     * @param permission Permission to revoke
     * @return Whether the permission was revoked successfully
     */
    public boolean revokePermission(Player player, String permission) {
        if (luckPermsEnabled) {
            return revokeLuckPermsPermission(player, permission);
        } else {
            // Fallback to Bukkit permissions
            UUID playerUuid = player.getUniqueId();

            if (playerPermissions.containsKey(playerUuid)) {
                Map<String, PermissionAttachment> playerPerms = playerPermissions.get(playerUuid);

                if (playerPerms.containsKey(permission)) {
                    PermissionAttachment attachment = playerPerms.get(permission);
                    player.removeAttachment(attachment);
                    playerPerms.remove(permission);

                    player.updateCommands();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Revokes a permission using LuckPerms
     *
     * @param player Player to revoke permission from
     * @param permission Permission to revoke
     * @return Whether the permission was revoked successfully
     */
    private boolean revokeLuckPermsPermission(Player player, String permission) {
        try {
            // Use modifyUser for atomic load-modify-save operations
            final boolean[] success = {false}; // Array to hold success state from lambda

            luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                // Find all nodes matching our permission
                Set<Node> toRemove = new HashSet<>();

                // Collect all permission nodes that match our key
                for (Node node : user.getNodes()) {
                    if (node.getType() == NodeType.PERMISSION &&
                            node.getKey().equalsIgnoreCase(permission)) {
                        toRemove.add(node);
                    }
                }

                // Remove each node
                boolean anySuccess = false;
                for (Node node : toRemove) {
                    DataMutateResult result = user.data().remove(node);
                    if (result.wasSuccessful()) {
                        anySuccess = true;
                    }
                }

                success[0] = anySuccess;

                if (success[0]) {
                    plugin.getLogger().info("Revoked permission " + permission + " from " + player.getName());
                }
            });

            return success[0];
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error revoking LuckPerms permission from " + player.getName(), e);
            return false;
        }
    }
    /**
     * Adds a timed permission
     *
     * @param timedPermission TimedPermission to add
     */
    public void addTimedPermission(TimedPermission timedPermission) {
        UUID playerUuid = timedPermission.getPlayerUuid();

        timedPermissions.putIfAbsent(playerUuid, new ArrayList<>());
        List<TimedPermission> playerTimedPerms = timedPermissions.get(playerUuid);

        // Remove existing timed permission with same node if present
        Iterator<TimedPermission> iterator = playerTimedPerms.iterator();
        while (iterator.hasNext()) {
            TimedPermission perm = iterator.next();
            if (perm.getPermission().equals(timedPermission.getPermission())) {
                iterator.remove();
            }
        }

        // Add new timed permission
        playerTimedPerms.add(timedPermission);

        // Grant permission to player if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            grantPermission(player, timedPermission.getPermission(), timedPermission.getExpirationTime());
        }
    }

    /**
     * Removes a timed permission
     *
     * @param playerUuid Player UUID
     * @param permission Permission to remove
     */
    public void removeTimedPermission(UUID playerUuid, String permission) {
        if (timedPermissions.containsKey(playerUuid)) {
            List<TimedPermission> playerTimedPerms = timedPermissions.get(playerUuid);

            // Remove timed permission
            Iterator<TimedPermission> iterator = playerTimedPerms.iterator();
            while (iterator.hasNext()) {
                TimedPermission perm = iterator.next();
                if (perm.getPermission().equals(permission)) {
                    iterator.remove();
                }
            }

            // Revoke permission from player if online
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                revokePermission(player, permission);
            }
        }
    }

    /**
     * Gets a player's timed permissions
     *
     * @param playerUuid Player UUID
     * @return List of timed permissions
     */
    public List<TimedPermission> getTimedPermissions(UUID playerUuid) {
        return timedPermissions.getOrDefault(playerUuid, new ArrayList<>());
    }

    /**
     * Checks if a player has a timed permission
     *
     * @param playerUuid Player UUID
     * @param permission Permission to check
     * @return Whether the player has the timed permission
     */
    public boolean hasTimedPermission(UUID playerUuid, String permission) {
        if (timedPermissions.containsKey(playerUuid)) {
            for (TimedPermission perm : timedPermissions.get(playerUuid)) {
                if (perm.getPermission().equals(permission) && !perm.hasExpired()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a timed permission
     *
     * @param playerUuid Player UUID
     * @param permission Permission to get
     * @return TimedPermission or null if not found
     */
    public TimedPermission getTimedPermission(UUID playerUuid, String permission) {
        if (timedPermissions.containsKey(playerUuid)) {
            for (TimedPermission perm : timedPermissions.get(playerUuid)) {
                if (perm.getPermission().equals(permission) && !perm.hasExpired()) {
                    return perm;
                }
            }
        }
        return null;
    }

    /**
     * Loads a player's permissions when they join
     *
     * @param player Player to load permissions for
     */
    public void loadPlayerPermissions(Player player) {
        UUID playerUuid = player.getUniqueId();

        if (timedPermissions.containsKey(playerUuid)) {
            List<TimedPermission> playerTimedPerms = timedPermissions.get(playerUuid);
            List<TimedPermission> expiredPerms = new ArrayList<>();

            // Process permissions
            for (TimedPermission timedPerm : playerTimedPerms) {
                if (timedPerm.hasExpired()) {
                    expiredPerms.add(timedPerm);

                    // Update purchase in database
                    plugin.getPurchaseManager().deactivatePurchase(timedPerm.getPurchaseId());
                } else {
                    // Grant permission
                    grantPermission(player, timedPerm.getPermission(), timedPerm.getExpirationTime());
                }
            }

            // Remove expired permissions
            playerTimedPerms.removeAll(expiredPerms);
        }
    }

    /**
     * Cleans up expired permissions
     */
    public void cleanupExpiredPermissions() {
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, List<TimedPermission>> entry : timedPermissions.entrySet()) {
            UUID playerUuid = entry.getKey();
            List<TimedPermission> playerTimedPerms = entry.getValue();
            List<TimedPermission> expiredPerms = new ArrayList<>();

            // Find expired permissions
            for (TimedPermission timedPerm : playerTimedPerms) {
                if (timedPerm.getExpirationTime() <= now) {
                    expiredPerms.add(timedPerm);

                    // Update purchase in database
                    plugin.getPurchaseManager().deactivatePurchase(timedPerm.getPurchaseId());

                    // Handle player operations
                    Player player = Bukkit.getPlayer(playerUuid);
                    if (player != null && player.isOnline()) {
                        // Revoke the permission
                        revokePermission(player, timedPerm.getPermission());

                        // If this was a flight permission, schedule disabling flight mode on the main thread
                        if (timedPerm.getPermission().equalsIgnoreCase("essentials.fly") ||
                                timedPerm.getPermission().endsWith(".fly")) {

                            // Schedule on main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (player.isOnline()) {
                                    if (player.isFlying() || player.getAllowFlight()) {
                                        // Disable flight mode
                                        player.setAllowFlight(false);
                                        player.setFlying(false);
                                        plugin.getMessages().sendPrefixedMessage(player, "permission.flight_disabled");
                                    }
                                }
                            });
                        }
                    }
                }
            }

            // Remove expired permissions from list
            playerTimedPerms.removeAll(expiredPerms);
        }
    }
    /**
     * Cleans up a player's permissions when they quit
     *
     * @param playerUuid Player UUID
     */
    public void cleanupPlayerPermissions(UUID playerUuid) {
        // With LuckPerms, permissions persist through server restarts
        // so we only need to clean up our Bukkit fallback permissions
        playerPermissions.remove(playerUuid);
    }

    /**
     * Checks if a player has a permission using LuckPerms CachedData
     *
     * @param player Player to check
     * @param permission Permission to check
     * @return Whether the player has the permission
     */
    public boolean hasPermission(Player player, String permission) {
        if (luckPermsEnabled) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } else {
            return player.hasPermission(permission);
        }
    }

    /**
     * Check if a player is in a specific group
     *
     * @param player Player to check
     * @param groupName Group name to check
     * @return Whether the player is in the group
     */
    public boolean isPlayerInGroup(Player player, String groupName) {
        if (luckPermsEnabled) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            return user.getCachedData().getPermissionData().checkPermission("group." + groupName).asBoolean();
        } else {
            return player.hasPermission("group." + groupName);
        }
    }

    /**
     * Get a player's primary group name
     *
     * @param player Player to get group for
     * @return Primary group name
     */
    public String getPlayerPrimaryGroup(Player player) {
        if (luckPermsEnabled) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            return user.getPrimaryGroup();
        } else {
            return "default"; // Fallback
        }
    }

    /**
     * Get all groups a player is in
     *
     * @param player Player to get groups for
     * @return Set of group names
     */
    public Set<String> getPlayerGroups(Player player) {
        if (luckPermsEnabled) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

            // Get user's inherited groups
            return user.getNodes(NodeType.INHERITANCE).stream()
                    .map(n -> n.getKey().substring(6)) // Remove "group." prefix
                    .collect(Collectors.toSet());
        } else {
            // Fallback - can't determine multiple groups easily
            return Collections.singleton(getPlayerPrimaryGroup(player));
        }
    }

    /**
     * Checks if LuckPerms is enabled
     *
     * @return Whether LuckPerms is enabled
     */
    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }

    /**
     * Gets the LuckPerms API
     *
     * @return LuckPerms API or null if not available
     */
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}