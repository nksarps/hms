package com.nks.hms.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * Promotes dependency inversion by allowing high-level modules to depend on abstractions.
 * 
 * @param <T> The entity type this repository manages
 */
public interface IRepository<T> {
    /**
     * Finds an entity by its unique identifier.
     * 
     * @param id The entity's primary key
     * @return Optional containing the entity if found, empty otherwise
     * @throws SQLException If database access fails
     */
    Optional<T> findById(int id) throws SQLException;
    
    /**
     * Searches for entities matching the search term with pagination.
     * 
     * @param searchTerm Text to search for (implementation-specific behavior)
     * @param limit Maximum number of results to return
     * @param offset Number of records to skip for pagination
     * @return List of matching entities (empty if none found)
     * @throws SQLException If database access fails
     */
    List<T> find(String searchTerm, int limit, int offset) throws SQLException;
    
    /**
     * Counts the total number of entities matching the search term.
     * 
     * @param searchTerm Text to search for (null or empty for total count)
     * @return Total count of matching entities
     * @throws SQLException If database access fails
     */
    int count(String searchTerm) throws SQLException;
    
    /**
     * Inserts a new entity into the repository.
     * 
     * @param entity The entity to insert (ID will be auto-generated)
     * @return The generated ID of the new entity
     * @throws SQLException If insertion fails
     */
    int insert(T entity) throws SQLException;
    
    /**
     * Updates an existing entity in the repository.
     * 
     * @param entity The entity with updated values (must have valid ID)
     * @throws SQLException If update fails
     */
    void update(T entity) throws SQLException;
    
    /**
     * Deletes an entity by its unique identifier.
     * 
     * @param id The ID of the entity to delete
     * @throws SQLException If deletion fails
     */
    void delete(int id) throws SQLException;
}
