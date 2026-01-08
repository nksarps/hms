package com.nks.hms.validation;

import com.nks.hms.model.MedicalInventory;
import java.util.Optional;

/**
 * Validator for MedicalInventory entities.
 * Implements single responsibility principle by separating validation logic.
 */
public class MedicalInventoryValidator implements IValidator<MedicalInventory> {
    
    @Override
    public Optional<String> validate(MedicalInventory item) {
        if (item == null) {
            return Optional.of("Medical inventory item cannot be null");
        }
        
        if (item.getName() == null || item.getName().isBlank()) {
            return Optional.of("Name is required");
        }
        
        if (item.getQuantity() == null) {
            return Optional.of("Quantity is required");
        }
        
        if (item.getQuantity() < 0) {
            return Optional.of("Quantity cannot be negative");
        }
        
        if (item.getCost() != null && item.getCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Optional.of("Cost cannot be negative");
        }
        
        return Optional.empty();
    }
}
