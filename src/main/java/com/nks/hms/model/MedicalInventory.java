package com.nks.hms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain model representing a medical inventory item in the hospital management system.
 * Contains information about medications, supplies, and equipment needed for patient care
 * and management. Maps directly to the 'MedicalInventory' table in the MySQL database.
 * 
 * @see com.nks.hms.repository.MedicalInventoryRepository
 */
public class MedicalInventory {
    private Integer id;
    private String name;
    private String type;
    private Integer quantity;
    private String unit;
    private LocalDate expiryDate;
    private BigDecimal cost;

    /**
     * Default no-arg constructor required by JavaFX for data binding
     * and by JDBC when creating instances from result sets.
     */
    public MedicalInventory() {
    }

    /**
     * Full constructor for creating a medical inventory item with all fields initialized.
     * Typically used when mapping database results to MedicalInventory objects.
     * 
     * @param id Database primary key, null for new items
     * @param name Item name (required)
     * @param type Item type/category (optional)
     * @param quantity Current quantity in stock (required)
     * @param unit Unit of measurement (e.g., "tablets", "bottles", "boxes") (optional)
     * @param expiryDate Expiry date of the item (optional)
     * @param cost Cost per unit (optional)
     */
    public MedicalInventory(Integer id, String name, String type, Integer quantity, 
                           String unit, LocalDate expiryDate, BigDecimal cost) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.cost = cost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "MedicalInventory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                ", cost=" + cost +
                '}';
    }
}
