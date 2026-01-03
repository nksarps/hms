package com.nks.hms.service;

import com.nks.hms.model.Patient;
import com.nks.hms.repository.PatientRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for patient data access with LRU eviction policy.
 * 
 * <p>Provides in-memory caching to reduce database load and improve response times
 * for frequently accessed patients. Implements Least Recently Used (LRU) eviction
 * when cache reaches maximum capacity.
 * 
 * <p>Key features:
 * <ul>
 *   <li>ID-based caching using LinkedHashMap for O(1) access and LRU ordering</li>
 *   <li>Search result caching with configurable TTL (time-to-live)</li>
 *   <li>Automatic cache invalidation on create/update/delete operations</li>
 *   <li>In-memory sorting by name, date of birth, or ID</li>
 *   <li>Configurable cache size limits</li>
 * </ul>
 * 
 * <p>Thread safety: This implementation is NOT thread-safe. For multi-threaded
 * environments, external synchronization or ConcurrentHashMap should be used.
 * 
 * @see com.nks.hms.repository.PatientRepository
 * @see com.nks.hms.model.Patient
 */
public class PatientCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000; // 1 minute
    
    private final PatientRepository repository;
    
    /** LRU cache for individual patients by ID */
    private final Map<Integer, Patient> patientCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting patient lists.
     */
    public enum SortBy {
        ID_ASC(Comparator.comparing(Patient::getId)),
        ID_DESC(Comparator.comparing(Patient::getId).reversed()),
        NAME_ASC(Comparator.comparing(Patient::getLastName)
                .thenComparing(Patient::getFirstName)),
        NAME_DESC(Comparator.comparing(Patient::getLastName)
                .thenComparing(Patient::getFirstName).reversed()),
        DOB_ASC(Comparator.comparing(Patient::getDateOfBirth, 
                Comparator.nullsLast(Comparator.naturalOrder()))),
        DOB_DESC(Comparator.comparing(Patient::getDateOfBirth,
                Comparator.nullsFirst(Comparator.reverseOrder())));
        
        private final Comparator<Patient> comparator;
        
        SortBy(Comparator<Patient> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<Patient> getComparator() {
            return comparator;
        }
    }
    
    /**
     * Wrapper for cached search results with timestamp for TTL management.
     */
    private static class CachedSearchResult {
        final List<Patient> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<Patient> results, int totalCount) {
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
     * @param repository The underlying patient repository for database access
     */
    public PatientCache(PatientRepository repository) {
        this.repository = repository;
        // LinkedHashMap with access-order for LRU behavior
        this.patientCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Patient> eldest) {
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
     * Retrieves a patient by ID, using cache if available.
     * Cache miss triggers database query and cache population.
     * 
     * @param id Patient's unique identifier
     * @return Optional containing the patient if found
     * @throws SQLException If database query fails
     */
    public Optional<Patient> findById(int id) throws SQLException {
        Patient cached = patientCache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<Patient> patient = repository.findById(id);
        patient.ifPresent(p -> patientCache.put(id, p));
        return patient;
    }
    
    /**
     * Searches for patients with optional sorting and caching.
     * Results are cached for subsequent identical searches.
     * 
     * @param searchTerm Search pattern (null or empty for all patients)
     * @param limit Maximum results to return
     * @param offset Records to skip (pagination)
     * @param sortBy Sort order (null for database default order)
     * @return List of matching patients in specified order
     * @throws SQLException If database query fails
     */
    public List<Patient> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String cacheKey = buildSearchCacheKey(searchTerm, limit, offset, sortBy);
        
        // Check cache and validate TTL
        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.results);
        }
        
        // Cache miss or expired - query database
        List<Patient> results = repository.find(searchTerm, limit, offset);
        
        // Apply in-memory sorting if requested
        if (sortBy != null && !results.isEmpty()) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        // Cache the results with metadata
        int totalCount = repository.count(searchTerm);
        searchCache.put(cacheKey, new CachedSearchResult(results, totalCount));
        
        // Also cache individual patients by ID for faster subsequent lookups
        results.forEach(p -> patientCache.put(p.getId(), p));
        
        return results;
    }
    
    /**
     * Counts total patients matching the search term.
     * Uses cached search results if available.
     * 
     * @param searchTerm Search pattern
     * @return Total number of matching records
     * @throws SQLException If database query fails
     */
    public int count(String searchTerm) throws SQLException {
        // Try to get count from cached search results
        for (Map.Entry<String, CachedSearchResult> entry : searchCache.entrySet()) {
            if (entry.getKey().startsWith(searchTerm + "|") && !entry.getValue().isExpired()) {
                return entry.getValue().totalCount;
            }
        }
        
        return repository.count(searchTerm);
    }
    
    /**
     * Inserts a new patient and invalidates relevant caches.
     * 
     * @param patient Patient to insert
     * @return Generated patient ID
     * @throws SQLException If insert fails
     */
    public int insert(Patient patient) throws SQLException {
        int id = repository.insert(patient);
        invalidateAllCaches();
        return id;
    }
    
    /**
     * Updates an existing patient and invalidates caches.
     * 
     * @param patient Patient with updated data
     * @throws SQLException If update fails
     */
    public void update(Patient patient) throws SQLException {
        repository.update(patient);
        invalidatePatientCache(patient.getId());
        invalidateSearchCache();
    }
    
    /**
     * Deletes a patient and invalidates caches.
     * 
     * @param id Patient ID to delete
     * @throws SQLException If delete fails
     */
    public void delete(int id) throws SQLException {
        repository.delete(id);
        invalidatePatientCache(id);
        invalidateSearchCache();
    }
    
    /**
     * Retrieves patient visit history (not cached due to frequent updates).
     * 
     * @param patientId Patient's ID
     * @return List of visit history records
     * @throws SQLException If query fails
     */
    public List<com.nks.hms.model.VisitHistory> getHistory(int patientId) throws SQLException {
        return repository.fetchHistory(patientId);
    }
    
    /**
     * Clears all cached data (both patient and search caches).
     */
    public void invalidateAllCaches() {
        patientCache.clear();
        searchCache.clear();
    }
    
    /**
     * Removes a specific patient from the cache.
     * 
     * @param id Patient ID to remove
     */
    private void invalidatePatientCache(int id) {
        patientCache.remove(id);
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
        return String.format("Patient cache: %d/%d, Search cache: %d/%d",
                patientCache.size(), MAX_CACHE_SIZE,
                searchCache.size(), MAX_SEARCH_CACHE_SIZE);
    }
}
