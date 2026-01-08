package com.nks.hms.service;

import com.nks.hms.model.Prescription;
import com.nks.hms.repository.IPrescriptionRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for prescription data access with LRU eviction policy.
 * 
 * <p>Provides in-memory caching to reduce database load and improve response times
 * for frequently accessed prescriptions. Implements Least Recently Used (LRU) eviction
 * when cache reaches maximum capacity.
 * 
 * <p>Key features:
 * <ul>
 *   <li>ID-based caching using LinkedHashMap for O(1) access and LRU ordering</li>
 *   <li>Search result caching with configurable TTL (time-to-live)</li>
 *   <li>Automatic cache invalidation on create/update/delete operations</li>
 *   <li>In-memory sorting by medication, prescribed date, or patient name</li>
 *   <li>Configurable cache size limits</li>
 * </ul>
 * 
 * <p>Thread safety: This implementation is NOT thread-safe. For multi-threaded
 * environments, external synchronization or ConcurrentHashMap should be used.
 * 
 * @see com.nks.hms.repository.PrescriptionRepository
 * @see com.nks.hms.model.Prescription
 */
public class PrescriptionCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000; // 1 minute
    
    private final IPrescriptionRepository repository;
    
    /** LRU cache for individual prescriptions by ID */
    private final Map<Integer, Prescription> prescriptionCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting prescription lists.
     */
    public enum SortBy {
        NONE(Comparator.comparing(Prescription::getId)),
        DATE_ASC(Comparator.comparing(Prescription::getPrescriptionDate, 
                Comparator.nullsLast(Comparator.naturalOrder()))),
        DATE_DESC(Comparator.comparing(Prescription::getPrescriptionDate,
                Comparator.nullsFirst(Comparator.reverseOrder()))),
        PATIENT_ASC(Comparator.comparing(Prescription::getPatientName)),
        PATIENT_DESC(Comparator.comparing(Prescription::getPatientName).reversed()),
        DOCTOR_ASC(Comparator.comparing(Prescription::getDoctorName)),
        DOCTOR_DESC(Comparator.comparing(Prescription::getDoctorName).reversed());
        
        private final Comparator<Prescription> comparator;
        
        SortBy(Comparator<Prescription> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<Prescription> getComparator() {
            return comparator;
        }
    }
    
    /**
     * Wrapper for cached search results with timestamp for TTL management.
     */
    private static class CachedSearchResult {
        final List<Prescription> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<Prescription> results, int totalCount) {
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
     * @param repository The underlying prescription repository for database access
     */
    public PrescriptionCache(IPrescriptionRepository repository) {
        this.repository = repository;
        this.prescriptionCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Prescription> eldest) {
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
     * Retrieves a prescription by ID, using cache if available.
     * Cache miss triggers database query and cache population.
     * 
     * @param id Prescription's unique identifier
     * @return Optional containing the prescription if found
     * @throws SQLException If database query fails
     */
    public Optional<Prescription> findById(int id) throws SQLException {
        Prescription cached = prescriptionCache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<Prescription> prescription = repository.findById(id);
        prescription.ifPresent(p -> prescriptionCache.put(id, p));
        return prescription;
    }
    
    /**
     * Searches for prescriptions with optional sorting and caching.
     * Results are cached for subsequent identical searches.
     * 
     * @param searchTerm Search pattern (null or empty for all prescriptions)
     * @param limit Maximum results to return
     * @param offset Records to skip (pagination)
     * @param sortBy Sort order (null for database default order)
     * @return List of matching prescriptions in specified order
     * @throws SQLException If database query fails
     */
    public List<Prescription> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String cacheKey = buildSearchCacheKey(searchTerm, limit, offset, sortBy);
        
        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.results);
        }
        
        List<Prescription> results = repository.find(searchTerm, limit, offset);
        
        if (sortBy != null && !results.isEmpty()) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        int totalCount = repository.count(searchTerm);
        searchCache.put(cacheKey, new CachedSearchResult(results, totalCount));
        
        results.forEach(p -> prescriptionCache.put(p.getId(), p));
        
        return results;
    }
    
    /**
     * Counts total prescriptions matching the search term.
     * Uses cached search results if available.
     * 
     * @param searchTerm Search pattern
     * @return Total number of matching records
     * @throws SQLException If database query fails
     */
    public int count(String searchTerm) throws SQLException {
        for (Map.Entry<String, CachedSearchResult> entry : searchCache.entrySet()) {
            if (entry.getKey().startsWith(searchTerm + "|") && !entry.getValue().isExpired()) {
                return entry.getValue().totalCount;
            }
        }
        
        return repository.count(searchTerm);
    }
    
    /**
     * Inserts a new prescription and invalidates relevant caches.
     * 
     * @param prescription Prescription to insert
     * @return Generated prescription ID
     * @throws SQLException If insert fails
     */
    public int insert(Prescription prescription) throws SQLException {
        int id = repository.insert(prescription);
        invalidateAllCaches();
        return id;
    }
    
    /**
     * Updates an existing prescription and invalidates caches.
     * 
     * @param prescription Prescription with updated data
     * @throws SQLException If update fails
     */
    public void update(Prescription prescription) throws SQLException {
        repository.update(prescription);
        invalidatePrescriptionCache(prescription.getId());
        invalidateSearchCache();
    }
    
    /**
     * Deletes a prescription and invalidates caches.
     * 
     * @param id Prescription ID to delete
     * @throws SQLException If delete fails
     */
    public void delete(int id) throws SQLException {
        repository.delete(id);
        invalidatePrescriptionCache(id);
        invalidateSearchCache();
    }
    
    /**
     * Clears all cached data (both prescription and search caches).
     */
    public void invalidateAllCaches() {
        prescriptionCache.clear();
        searchCache.clear();
    }
    
    /**
     * Removes a specific prescription from the cache.
     * 
     * @param id Prescription ID to remove
     */
    private void invalidatePrescriptionCache(int id) {
        prescriptionCache.remove(id);
    }
    
    /**
     * Clears all search result caches.
     * Called when data is modified to prevent stale results.
     */
    private void invalidateSearchCache() {
        searchCache.clear();
    }
    
    /**
     * Builds a unique cache key for search parameters.
     * 
     * @param searchTerm Search pattern
     * @param limit Result limit
     * @param offset Result offset
     * @param sortBy Sort order
     * @return Composite cache key string
     */
    private String buildSearchCacheKey(String searchTerm, int limit, int offset, SortBy sortBy) {
        String term = searchTerm == null ? "" : searchTerm;
        String sort = sortBy == null ? "default" : sortBy.name();
        return term + "|" + limit + "|" + offset + "|" + sort;
    }
    
    /**
     * Returns current cache statistics for monitoring.
     * 
     * @return String with cache size information
     */
    public String getCacheStats() {
        return String.format("Prescription cache: %d/%d, Search cache: %d/%d",
                prescriptionCache.size(), MAX_CACHE_SIZE,
                searchCache.size(), MAX_SEARCH_CACHE_SIZE);
    }
}
