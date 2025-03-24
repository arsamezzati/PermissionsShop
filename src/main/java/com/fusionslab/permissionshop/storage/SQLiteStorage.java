package com.fusionslab.permissionshop.storage;

import com.fusionslab.permissionshop.PermissionsShop;
import com.fusionslab.permissionshop.models.Purchase;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SQLite implementation of StorageManager
 */
public class SQLiteStorage implements StorageManager {

    private final PermissionsShop plugin;
    private Connection connection;

    public SQLiteStorage(PermissionsShop plugin) {
        this.plugin = plugin;
        initialize();
    }

    @Override
    public void initialize() {
        try {
            // Create database directory if it doesn't exist
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to database
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "permissionshop.db"));

            // Create tables
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize SQLite database", e);
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not close SQLite connection", e);
        }
    }

    /**
     * Creates database tables
     */
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Create purchases table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS purchases (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "player_uuid VARCHAR(36) NOT NULL," +
                            "item_id VARCHAR(64) NOT NULL," +
                            "purchase_time BIGINT NOT NULL," +
                            "expiration_time BIGINT NOT NULL," +
                            "remaining_uses INT NOT NULL," +
                            "active BOOLEAN NOT NULL" +
                            ");"
            );

            // Create index for player_uuid
            statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_player_uuid ON purchases (player_uuid);"
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create tables", e);
        }
    }

    @Override
    public int savePurchase(Purchase purchase) {
        String sql = "INSERT INTO purchases (player_uuid, item_id, purchase_time, expiration_time, remaining_uses, active) VALUES (?, ?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, purchase.getPlayerUuid().toString());
            statement.setString(2, purchase.getItemId());
            statement.setLong(3, purchase.getPurchaseTime());
            statement.setLong(4, purchase.getExpirationTime());
            statement.setInt(5, purchase.getRemainingUses());
            statement.setBoolean(6, purchase.isActive());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save purchase", e);
        }

        return -1;
    }

    @Override
    public List<Purchase> getPlayerPurchases(UUID playerUuid) {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE player_uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    purchases.add(parsePurchase(resultSet));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get player purchases", e);
        }

        return purchases;
    }

    @Override
    public Purchase getPurchase(int id) {
        String sql = "SELECT * FROM purchases WHERE id = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return parsePurchase(resultSet);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get purchase", e);
        }

        return null;
    }

    @Override
    public boolean deactivatePurchase(int id) {
        String sql = "UPDATE purchases SET active = FALSE WHERE id = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not deactivate purchase", e);
        }

        return false;
    }

    @Override
    public boolean updatePurchaseUses(int id, int remainingUses) {
        String sql = "UPDATE purchases SET remaining_uses = ? WHERE id = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, remainingUses);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not update purchase uses", e);
        }

        return false;
    }

    /**
     * Parses a purchase from a ResultSet
     *
     * @param resultSet ResultSet to parse
     * @return Parsed Purchase
     * @throws SQLException if an error occurs
     */
    private Purchase parsePurchase(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        UUID playerUuid = UUID.fromString(resultSet.getString("player_uuid"));
        String itemId = resultSet.getString("item_id");
        long purchaseTime = resultSet.getLong("purchase_time");
        long expirationTime = resultSet.getLong("expiration_time");
        int remainingUses = resultSet.getInt("remaining_uses");
        boolean active = resultSet.getBoolean("active");

        return new Purchase(id, playerUuid, itemId, purchaseTime, expirationTime, remainingUses, active);
    }
}