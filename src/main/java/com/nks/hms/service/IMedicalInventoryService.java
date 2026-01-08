package com.nks.hms.service;

import com.nks.hms.model.MedicalInventory;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for medical inventory business operations.
 * Abstracts business logic away from UI and repository layers.
 * Follows dependency inversion principle.
 */
public interface IMedicalInventoryService {
    /**
     * Searches for medical inventory items with pagination and sorting.
     * 
     * @param searchTerm Search text (null/empty for all)
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching items
     * @throws SQLException If database access fails
     */
    List<MedicalInventory> searchInventory(String searchTerm, int limit, int offset, String sortBy) throws SQLException;
    
    /**
     * Counts total items matching search criteria.
     * 
     * @param searchTerm Search text
     * @return Total count
     * @throws SQLException If database access fails
     */
    int countInventory(String searchTerm) throws SQLException;
    
    /**
     * Finds an inventory item by ID.
     * 
     * @param id Item ID
     * @return Optional containing item if found
     * @throws SQLException If database access fails
     */
    Optional<MedicalInventory> getInventoryById(int id) throws SQLException;
    
    /**
     * Saves a new item or updates an existing one.
     * 
     * @param item Item to save
     * @return Generated ID for new items, existing ID for updates
     * @throws SQLException If save fails
     */
    int saveInventory(MedicalInventory item) throws SQLException;
    
    /**
     * Deletes an item by ID.
     * 
     * @param id Item ID to delete
     * @throws SQLException If deletion fails
     */
    void deleteInventory(int id) throws SQLException;
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return Cache statistics string
     */
    String getCacheStats();
}
