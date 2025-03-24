package com.fusionslab.permissionshop.storage;

import com.fusionslab.permissionshop.models.Purchase;

import java.util.List;
import java.util.UUID;

/**
 * Interface for storage managers
 */
public interface StorageManager {

    /**
     * Initializes the storage
     */
    void initialize();

    /**
     * Closes the connection
     */
    void closeConnection();

    /**
     * Saves a purchase to storage
     *
     * @param purchase Purchase to save
     * @return Database ID of the purchase or -1 if failed
     */
    int savePurchase(Purchase purchase);

    /**
     * Gets all purchases for a player
     *
     * @param playerUuid Player UUID
     * @return List of purchases
     */
    List<Purchase> getPlayerPurchases(UUID playerUuid);

    /**
     * Gets a purchase by ID
     *
     * @param id Purchase ID
     * @return Purchase or null if not found
     */
    Purchase getPurchase(int id);

    /**
     * Deactivates a purchase
     *
     * @param id Purchase ID
     * @return Whether the operation was successful
     */
    boolean deactivatePurchase(int id);

    /**
     * Updates a purchase's remaining uses
     *
     * @param id Purchase ID
     * @param remainingUses Remaining uses
     * @return Whether the operation was successful
     */
    boolean updatePurchaseUses(int id, int remainingUses);
}