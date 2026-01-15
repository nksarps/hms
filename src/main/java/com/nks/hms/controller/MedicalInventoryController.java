package com.nks.hms.controller;

import com.nks.hms.model.MedicalInventory;
import com.nks.hms.service.IMedicalInventoryService;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for medical inventory UI operations.
 * Separates UI logic from Main class (Single Responsibility Principle).
 * Depends on IMedicalInventoryService abstraction (Dependency Inversion Principle).
 */
public class MedicalInventoryController {
    private static final int PAGE_SIZE = 25;
    private final IMedicalInventoryService inventoryService;
    
    public MedicalInventoryController(IMedicalInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * Loads inventory items into table with pagination and sorting.
     */
    public void loadInventory(TableView<MedicalInventory> table, Pagination pagination, 
                             String searchTerm, String sortBy, Label feedback) {
        try {
            int total = inventoryService.countInventory(searchTerm);
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            pagination.setPageCount(pages);
            
            int current = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            pagination.setCurrentPageIndex(current);
            int offset = current * PAGE_SIZE;
            
            List<MedicalInventory> items = inventoryService.searchInventory(searchTerm, PAGE_SIZE, offset, sortBy);
            table.setItems(FXCollections.observableArrayList(items));
            
            feedback.setText(total + " items found");
            feedback.setStyle("-fx-text-fill: #006400;");
        } catch (SQLException ex) {
            feedback.setText("Failed to load inventory: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Saves or updates an inventory item.
     */
    public void saveInventory(TableView<MedicalInventory> table, Pagination pagination, 
                             TextField searchField, ComboBox<String> sortCombo, Label feedback,
                             TextField nameField, TextField typeField, TextField quantityField,
                             TextField unitField, DatePicker expiryPicker, TextField costField) {
        MedicalInventory selected = table.getSelectionModel().getSelectedItem();
        boolean isNew = selected == null;
        
        if (isNew) {
            selected = new MedicalInventory();
        }
        
        selected.setName(nameField.getText());
        selected.setType(typeField.getText());
        
        // Parse quantity
        try {
            if (quantityField.getText() != null && !quantityField.getText().isBlank()) {
                selected.setQuantity(Integer.parseInt(quantityField.getText()));
            } else {
                selected.setQuantity(0);
            }
        } catch (NumberFormatException ex) {
            feedback.setText("Invalid quantity format");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        selected.setUnit(unitField.getText());
        selected.setExpiryDate(expiryPicker.getValue());
        
        // Parse cost
        try {
            if (costField.getText() != null && !costField.getText().isBlank()) {
                selected.setCost(new BigDecimal(costField.getText()));
            } else {
                selected.setCost(null);
            }
        } catch (NumberFormatException ex) {
            feedback.setText("Invalid cost format");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            int id = inventoryService.saveInventory(selected);
            selected.setId(id);
            feedback.setText(isNew ? "Item saved" : "Item updated");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadInventory(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(nameField, typeField, quantityField, unitField, expiryPicker, costField);
            table.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            feedback.setText(ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        } catch (SQLException ex) {
            feedback.setText("Failed to save item: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Deletes an inventory item.
     */
    public void deleteInventory(TableView<MedicalInventory> table, Pagination pagination,
                               TextField searchField, ComboBox<String> sortCombo, Label feedback,
                               TextField nameField, TextField typeField, TextField quantityField,
                               TextField unitField, DatePicker expiryPicker, TextField costField) {
        MedicalInventory selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedback.setText("Select an item to delete");
            feedback.setStyle("-fx-text-fill: #B00020;");
            return;
        }
        
        try {
            inventoryService.deleteInventory(selected.getId());
            feedback.setText("Item deleted");
            feedback.setStyle("-fx-text-fill: #006400;");
            loadInventory(table, pagination, searchField.getText(), sortCombo.getValue(), feedback);
            clearForm(nameField, typeField, quantityField, unitField, expiryPicker, costField);
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            feedback.setText("Failed to delete item: " + ex.getMessage());
            feedback.setStyle("-fx-text-fill: #B00020;");
        }
    }
    
    /**
     * Populates form from selected item.
     */
    public void populateForm(MedicalInventory item, TextField nameField, TextField typeField,
                            TextField quantityField, TextField unitField, DatePicker expiryPicker,
                            TextField costField) {
        if (item == null) {
            clearForm(nameField, typeField, quantityField, unitField, expiryPicker, costField);
            return;
        }
        nameField.setText(item.getName());
        typeField.setText(item.getType());
        quantityField.setText(item.getQuantity() != null ? item.getQuantity().toString() : "");
        unitField.setText(item.getUnit());
        expiryPicker.setValue(item.getExpiryDate());
        costField.setText(item.getCost() != null ? item.getCost().toString() : "");
    }
    
    /**
     * Clears form fields.
     */
    public void clearForm(TextField nameField, TextField typeField, TextField quantityField,
                         TextField unitField, DatePicker expiryPicker, TextField costField) {
        nameField.clear();
        typeField.clear();
        quantityField.clear();
        unitField.clear();
        expiryPicker.setValue(null);
        costField.clear();
    }
}
