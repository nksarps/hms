package com.nks.hms.service;

import com.nks.hms.model.Doctor;
import com.nks.hms.repository.DoctorRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for doctor data access with LRU eviction policy.
 * 
 * <p>Similar to PatientCache but simplified as doctors don't have visit history.
 * Provides in-memory caching to reduce database load and improve response times.
 * 
 * <p>Key features:
 * <ul>
 *   <li>ID-based caching with LRU eviction</li>
 *   <li>Search result caching with TTL</li>
 *   <li>Automatic cache invalidation on modifications</li>
 *   <li>In-memory sorting by name or ID</li>
 * </ul>
 * 
 * @see com.nks.hms.repository.DoctorRepository
 * @see com.nks.hms.model.Doctor
 */
public class DoctorCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000;
    
    private final DoctorRepository repository;
    private final Map<Integer, Doctor> doctorCache;
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Sorting options for doctor lists.
     */
    public enum SortBy {
        ID_ASC(Comparator.comparing(Doctor::getId)),
        ID_DESC(Comparator.comparing(Doctor::getId).reversed()),
        NAME_ASC(Comparator.comparing(Doctor::getLastName)
                .thenComparing(Doctor::getFirstName)),
        NAME_DESC(Comparator.comparing(Doctor::getLastName)
                .thenComparing(Doctor::getFirstName).reversed());
        
        private final Comparator<Doctor> comparator;
        
        SortBy(Comparator<Doctor> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<Doctor> getComparator() {
            return comparator;
        }
    }
    
    private static class CachedSearchResult {
        final List<Doctor> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<Doctor> results, int totalCount) {
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
            this.totalCount = totalCount;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > SEARCH_CACHE_TTL_MS;
        }
    }
    
    public DoctorCache(DoctorRepository repository) {
        this.repository = repository;
        this.doctorCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Doctor> eldest) {
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
    
    public Optional<Doctor> findById(int id) throws SQLException {
        Doctor cached = doctorCache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<Doctor> doctor = repository.findById(id);
        doctor.ifPresent(d -> doctorCache.put(id, d));
        return doctor;
    }
    
    public List<Doctor> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String cacheKey = buildSearchCacheKey(searchTerm, limit, offset, sortBy);
        
        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.results);
        }
        
        List<Doctor> results = repository.find(searchTerm, limit, offset);
        
        if (sortBy != null && !results.isEmpty()) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        int totalCount = repository.count(searchTerm);
        searchCache.put(cacheKey, new CachedSearchResult(results, totalCount));
        results.forEach(d -> doctorCache.put(d.getId(), d));
        
        return results;
    }
    
    public int count(String searchTerm) throws SQLException {
        for (Map.Entry<String, CachedSearchResult> entry : searchCache.entrySet()) {
            if (entry.getKey().startsWith(searchTerm + "|") && !entry.getValue().isExpired()) {
                return entry.getValue().totalCount;
            }
        }
        
        return repository.count(searchTerm);
    }
    
    public int insert(Doctor doctor) throws SQLException {
        int id = repository.insert(doctor);
        invalidateAllCaches();
        return id;
    }
    
    public void update(Doctor doctor) throws SQLException {
        repository.update(doctor);
        invalidateDoctorCache(doctor.getId());
        invalidateSearchCache();
    }
    
    public void delete(int id) throws SQLException {
        repository.delete(id);
        invalidateDoctorCache(id);
        invalidateSearchCache();
    }
    
    public void invalidateAllCaches() {
        doctorCache.clear();
        searchCache.clear();
    }
    
    private void invalidateDoctorCache(int id) {
        doctorCache.remove(id);
    }
    
    private void invalidateSearchCache() {
        searchCache.clear();
    }
    
    private String buildSearchCacheKey(String searchTerm, int limit, int offset, SortBy sortBy) {
        String term = searchTerm == null ? "" : searchTerm;
        String sort = sortBy == null ? "default" : sortBy.name();
        return term + "|" + limit + "|" + offset + "|" + sort;
    }
    
    public String getCacheStats() {
        return String.format("Doctor cache: %d/%d, Search cache: %d/%d",
                doctorCache.size(), MAX_CACHE_SIZE,
                searchCache.size(), MAX_SEARCH_CACHE_SIZE);
    }
}
