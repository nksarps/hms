package com.nks.hms.service;

import com.nks.hms.model.MedicalInventory;
import com.nks.hms.repository.IMedicalInventoryRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for medical inventory data access with LRU eviction policy.
 * 
 * <p>Provides in-memory caching to reduce database load and improve response times
 * for frequently accessed items. Implements Least Recently Used (LRU) eviction
 * when cache reaches maximum capacity.
 */
public class MedicalInventoryCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000; // 1 minute
    
    private final IMedicalInventoryRepository repository;
    
    /** LRU cache for individual items by ID */
    private final Map<Integer, MedicalInventory> itemCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting inventory lists.
     */
    public enum SortBy {
        NONE(Comparator.comparing(MedicalInventory::getId)),
        NAME_ASC(Comparator.comparing(MedicalInventory::getName)),
        NAME_DESC(Comparator.comparing(MedicalInventory::getName).reversed()),
        QUANTITY_ASC(Comparator.comparing(MedicalInventory::getQuantity, 
                Comparator.nullsLast(Comparator.naturalOrder()))),
        QUANTITY_DESC(Comparator.comparing(MedicalInventory::getQuantity,
                Comparator.nullsFirst(Comparator.reverseOrder()))),
        EXPIRY_ASC(Comparator.comparing(MedicalInventory::getExpiryDate, 
                Comparator.nullsLast(Comparator.naturalOrder()))),
        EXPIRY_DESC(Comparator.comparing(MedicalInventory::getExpiryDate,
                Comparator.nullsFirst(Comparator.reverseOrder())));
        
        private final Comparator<MedicalInventory> comparator;
        
        SortBy(Comparator<MedicalInventory> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<MedicalInventory> getComparator() {
            return comparator;
        }
    }
    
    /**
     * Wrapper for cached search results with timestamp for TTL management.
     */
    private static class CachedSearchResult {
        final List<MedicalInventory> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<MedicalInventory> results, int totalCount) {
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
            this.totalCount = totalCount;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > SEARCH_CACHE_TTL_MS;
        }
    }
    
    /**
     * Creates a new cache instance wrapping the given repository.
     * 
     * @param repository The underlying inventory repository for database access
     */
    public MedicalInventoryCache(IMedicalInventoryRepository repository) {
        this.repository = repository;
        this.itemCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, MedicalInventory> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        this.searchCache = new LinkedHashMap<>(MAX_SEARCH_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedSearchResult> eldest) {
                return size() > MAX_SEARCH_CACHE_SIZE;
            }
        };
    }
    
    /**
     * Retrieves an item by ID, using cache if available.
     * 
     * @param id Item's unique identifier
     * @return Optional containing the item if found
     * @throws SQLException If database query fails
     */
    public Optional<MedicalInventory> findById(int id) throws SQLException {
        MedicalInventory cached = itemCache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<MedicalInventory> item = repository.findById(id);
        item.ifPresent(i -> itemCache.put(id, i));
        return item;
    }
    
    /**
     * Searches for items with optional sorting and caching.
     * 
     * @param searchTerm Search pattern (null or empty for all items)
     * @param limit Maximum results to return
     * @param offset Records to skip (pagination)
     * @param sortBy Sort order (null for database default order)
     * @return List of matching items in specified order
     * @throws SQLException If database query fails
     */
    public List<MedicalInventory> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String searchKey = (searchTerm == null ? "" : searchTerm) + "|" + limit + "|" + offset + "|" + sortBy;
        
        CachedSearchResult cached = searchCache.get(searchKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.results);
        }
        
        List<MedicalInventory> results = repository.find(searchTerm, limit, offset);
        
        if (sortBy != null && sortBy != SortBy.NONE) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        for (MedicalInventory item : results) {
            itemCache.put(item.getId(), item);
        }
        
        int totalCount = repository.count(searchTerm);
        searchCache.put(searchKey, new CachedSearchResult(results, totalCount));
        
        return results;
    }
    
    /**
     * Counts total items matching search criteria.
     * 
     * @param searchTerm Search pattern
     * @return Total matching count
     * @throws SQLException If database query fails
     */
    public int count(String searchTerm) throws SQLException {
        return repository.count(searchTerm);
    }
    
    /**
     * Inserts a new item and invalidates search cache.
     * 
     * @param item Item to insert
     * @return Generated database ID
     * @throws SQLException If insert fails
     */
    public int insert(MedicalInventory item) throws SQLException {
        int id = repository.insert(item);
        searchCache.clear();
        return id;
    }
    
    /**
     * Updates an existing item and invalidates caches.
     * 
     * @param item Item to update (must have valid ID)
     * @throws SQLException If update fails
     */
    public void update(MedicalInventory item) throws SQLException {
        repository.update(item);
        itemCache.remove(item.getId());
        searchCache.clear();
    }
    
    /**
     * Deletes an item and invalidates caches.
     * 
     * @param id ID of item to delete
     * @throws SQLException If deletion fails
     */
    public void delete(int id) throws SQLException {
        repository.delete(id);
        itemCache.remove(id);
        searchCache.clear();
    }
    
    /**
     * Returns cache statistics for monitoring.
     * 
     * @return String describing current cache state
     */
    public String getCacheStats() {
        return String.format("Items: %d/%d | Searches: %d/%d",
                itemCache.size(), MAX_CACHE_SIZE,
                searchCache.size(), MAX_SEARCH_CACHE_SIZE);
    }
}
