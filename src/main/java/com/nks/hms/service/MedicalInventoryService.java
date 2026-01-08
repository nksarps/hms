package com.nks.hms.service;

import com.nks.hms.model.MedicalInventory;
import com.nks.hms.repository.IMedicalInventoryRepository;
import com.nks.hms.validation.IValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Medical inventory service implementation providing business logic.
 * Wraps MedicalInventoryCache and adds validation layer.
 * Follows single responsibility and dependency inversion principles.
 */
public class MedicalInventoryService implements IMedicalInventoryService {
    private final MedicalInventoryCache cache;
    private final IValidator<MedicalInventory> validator;
    
    /**
     * Creates medical inventory service with dependency injection.
     * 
     * @param repository Medical inventory repository for data access
     * @param validator Medical inventory validator for business rules
     */
    public MedicalInventoryService(IMedicalInventoryRepository repository, IValidator<MedicalInventory> validator) {
        this.cache = new MedicalInventoryCache(repository);
        this.validator = validator;
    }
    
    @Override
    public List<MedicalInventory> searchInventory(String searchTerm, int limit, int offset, String sortBy) throws SQLException {
        MedicalInventoryCache.SortBy sortOption = parseSortBy(sortBy);
        return cache.find(searchTerm, limit, offset, sortOption);
    }
    
    @Override
    public int countInventory(String searchTerm) throws SQLException {
        return cache.count(searchTerm);
    }
    
    @Override
    public Optional<MedicalInventory> getInventoryById(int id) throws SQLException {
        return cache.findById(id);
    }
    
    @Override
    public int saveInventory(MedicalInventory item) throws SQLException {
        // Validate before saving
        Optional<String> error = validator.validate(item);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        
        // Trim string fields
        item.setName(item.getName().trim());
        if (item.getType() != null && !item.getType().isBlank()) {
            item.setType(item.getType().trim());
        }
        if (item.getUnit() != null && !item.getUnit().isBlank()) {
            item.setUnit(item.getUnit().trim());
        }
        
        // Insert or update
        if (item.getId() == null) {
            return cache.insert(item);
        } else {
            cache.update(item);
            return item.getId();
        }
    }
    
    @Override
    public void deleteInventory(int id) throws SQLException {
        cache.delete(id);
    }
    
    @Override
    public String getCacheStats() {
        return cache.getCacheStats();
    }
    
    /**
     * Converts UI sort string to cache sort enum.
     */
    private MedicalInventoryCache.SortBy parseSortBy(String uiValue) {
        if (uiValue == null) return MedicalInventoryCache.SortBy.NONE;
        return switch (uiValue) {
            case "All" -> MedicalInventoryCache.SortBy.NONE;
            case "Name (A-Z)" -> MedicalInventoryCache.SortBy.NAME_ASC;
            case "Name (Z-A)" -> MedicalInventoryCache.SortBy.NAME_DESC;
            case "Quantity (Low-High)" -> MedicalInventoryCache.SortBy.QUANTITY_ASC;
            case "Quantity (High-Low)" -> MedicalInventoryCache.SortBy.QUANTITY_DESC;
            case "Expiry (Soonest)" -> MedicalInventoryCache.SortBy.EXPIRY_ASC;
            case "Expiry (Latest)" -> MedicalInventoryCache.SortBy.EXPIRY_DESC;
            default -> MedicalInventoryCache.SortBy.NONE;
        };
    }
}
