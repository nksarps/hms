package com.nks.hms.service;

import com.nks.hms.model.Patient;
import com.nks.hms.repository.IPatientRepository;
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
    
    private final IPatientRepository repository;
    
    /** LRU cache for individual patients by ID */
    private final Map<Integer, Patient> patientCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting patient lists.
     */
    public enum SortBy {
        NONE(Comparator.comparing(Patient::getId)),
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
    public PatientCache(IPatientRepository repository) {
        this.repository = repository;
        // LinkedHashMap with access-order (true) enables LRU behavior
        // When an entry is accessed via get(), it moves to the end of insertion order
        // removeEldestEntry automatically removes least recently used when size exceeds limit
        // This prevents unbounded memory growth while keeping hot data in cache
        this.patientCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Patient> eldest) {
                // Keep cache size <= MAX_CACHE_SIZE, evict oldest entry when over limit
                return size() > MAX_CACHE_SIZE;
            }
        };
        // Search result cache with separate size limit and LRU eviction
        // Stores complete search query results with TTL for freshness
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
        // Build unique cache key from all search parameters (pagination + sort order matters)
        String cacheKey = buildSearchCacheKey(searchTerm, limit, offset, sortBy);
        
        // Check if result is already cached and not expired
        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            // Cache hit: return a defensive copy to prevent external modification of cache
            return new ArrayList<>(cached.results);
        }
        
        // Cache miss or TTL expired: query database (may hit numeric ID optimization)
        List<Patient> results = repository.find(searchTerm, limit, offset);
        
        // Apply in-memory sorting if requested
        // More efficient than re-sorting in database for cached results
        // Uses chainable Comparators: last name -> first name for stable multi-field sort
        if (sortBy != null && !results.isEmpty()) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        // Get total count for pagination (needed for page calculation)
        int totalCount = repository.count(searchTerm);
        // Store in search cache with current timestamp for TTL validation
        searchCache.put(cacheKey, new CachedSearchResult(results, totalCount));
        
        // Secondary benefit: populate individual patient cache from search results
        // Subsequent ID lookups for these patients will be O(1) from patientCache
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
        // Optimization: try to get count from cache instead of executing COUNT(*) query
        // Iterate through cached search results looking for a valid match
        for (Map.Entry<String, CachedSearchResult> entry : searchCache.entrySet()) {
            // Check if this cache entry is for the same search term and not expired
            if (entry.getKey().startsWith(searchTerm + "|") && !entry.getValue().isExpired()) {
                // Return cached total count (avoids database query)
                return entry.getValue().totalCount;
            }
        }
        
        // No valid cached result found, must query database
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
        // Insert into database first
        int id = repository.insert(patient);
        // Full cache invalidation: new patient won't appear in existing searches otherwise
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
        // Update in database
        repository.update(patient);
        // Conservative approach: remove only this patient from ID cache
        invalidatePatientCache(patient.getId());
        // Also clear search results since they may contain the modified patient
        invalidateSearchCache();
    }
    
    /**
     * Deletes a patient and invalidates caches.
     * 
     * @param id Patient ID to delete
     * @throws SQLException If delete fails
     */
    public void delete(int id) throws SQLException {
        // Delete from database
        repository.delete(id);
        // Remove from individual patient cache
        invalidatePatientCache(id);
        // Clear search results since they may contain the deleted patient
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
        return repository.getVisitHistory(patientId);
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
