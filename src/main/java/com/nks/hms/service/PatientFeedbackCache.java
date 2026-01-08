package com.nks.hms.service;

import com.nks.hms.model.PatientFeedback;
import com.nks.hms.repository.IPatientFeedbackRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for patient feedback data access with LRU eviction policy.
 * 
 * <p>Provides in-memory caching to reduce database load and improve response times
 * for frequently accessed feedback. Implements Least Recently Used (LRU) eviction
 * when cache reaches maximum capacity.
 */
public class PatientFeedbackCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000; // 1 minute
    
    private final IPatientFeedbackRepository repository;
    
    /** LRU cache for individual feedback by ID */
    private final Map<Integer, PatientFeedback> feedbackCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting feedback lists.
     */
    public enum SortBy {
        NONE(Comparator.comparing(PatientFeedback::getId)),
        DATE_ASC(Comparator.comparing(PatientFeedback::getFeedbackDate, 
                Comparator.nullsLast(Comparator.naturalOrder()))),
        DATE_DESC(Comparator.comparing(PatientFeedback::getFeedbackDate,
                Comparator.nullsFirst(Comparator.reverseOrder()))),
        PATIENT_ASC(Comparator.comparing(PatientFeedback::getPatientName)),
        PATIENT_DESC(Comparator.comparing(PatientFeedback::getPatientName).reversed()),
        DOCTOR_ASC(Comparator.comparing(PatientFeedback::getDoctorName)),
        DOCTOR_DESC(Comparator.comparing(PatientFeedback::getDoctorName).reversed()),
        RATING_ASC(Comparator.comparing(PatientFeedback::getRating,
                Comparator.nullsLast(Comparator.naturalOrder()))),
        RATING_DESC(Comparator.comparing(PatientFeedback::getRating,
                Comparator.nullsFirst(Comparator.reverseOrder())));
        
        private final Comparator<PatientFeedback> comparator;
        
        SortBy(Comparator<PatientFeedback> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<PatientFeedback> getComparator() {
            return comparator;
        }
    }
    
    /**
     * Wrapper for cached search results with timestamp for TTL management.
     */
    private static class CachedSearchResult {
        final List<PatientFeedback> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<PatientFeedback> results, int totalCount) {
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
     * @param repository The underlying feedback repository for database access
     */
    public PatientFeedbackCache(IPatientFeedbackRepository repository) {
        this.repository = repository;
        this.feedbackCache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, PatientFeedback> eldest) {
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
     * Retrieves a feedback by ID, using cache if available.
     * 
     * @param id Feedback's unique identifier
     * @return Optional containing the feedback if found
     * @throws SQLException If database query fails
     */
    public Optional<PatientFeedback> findById(int id) throws SQLException {
        PatientFeedback cached = feedbackCache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<PatientFeedback> feedback = repository.findById(id);
        feedback.ifPresent(f -> feedbackCache.put(id, f));
        return feedback;
    }
    
    /**
     * Searches for feedback with optional sorting and caching.
     * 
     * @param searchTerm Search pattern (null or empty for all feedback)
     * @param limit Maximum results to return
     * @param offset Records to skip (pagination)
     * @param sortBy Sort order (null for database default order)
     * @return List of matching feedback in specified order
     * @throws SQLException If database query fails
     */
    public List<PatientFeedback> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String searchKey = (searchTerm == null ? "" : searchTerm) + "|" + limit + "|" + offset + "|" + sortBy;
        
        CachedSearchResult cached = searchCache.get(searchKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.results);
        }
        
        List<PatientFeedback> results = repository.find(searchTerm, limit, offset);
        
        if (sortBy != null && sortBy != SortBy.NONE) {
            results = results.stream()
                    .sorted(sortBy.getComparator())
                    .collect(Collectors.toList());
        }
        
        for (PatientFeedback feedback : results) {
            feedbackCache.put(feedback.getId(), feedback);
        }
        
        int totalCount = repository.count(searchTerm);
        searchCache.put(searchKey, new CachedSearchResult(results, totalCount));
        
        return results;
    }
    
    /**
     * Counts total feedback matching search criteria.
     * 
     * @param searchTerm Search pattern
     * @return Total matching count
     * @throws SQLException If database query fails
     */
    public int count(String searchTerm) throws SQLException {
        return repository.count(searchTerm);
    }
    
    /**
     * Inserts a new feedback and invalidates search cache.
     * 
     * @param feedback Feedback to insert
     * @return Generated database ID
     * @throws SQLException If insert fails
     */
    public int insert(PatientFeedback feedback) throws SQLException {
        int id = repository.insert(feedback);
        searchCache.clear();
        return id;
    }
    
    /**
     * Updates an existing feedback and invalidates caches.
     * 
     * @param feedback Feedback to update (must have valid ID)
     * @throws SQLException If update fails
     */
    public void update(PatientFeedback feedback) throws SQLException {
        repository.update(feedback);
        feedbackCache.remove(feedback.getId());
        searchCache.clear();
    }
    
    /**
     * Deletes a feedback and invalidates caches.
     * 
     * @param id ID of feedback to delete
     * @throws SQLException If deletion fails
     */
    public void delete(int id) throws SQLException {
        repository.delete(id);
        feedbackCache.remove(id);
        searchCache.clear();
    }
    
    /**
     * Returns cache statistics for monitoring.
     * 
     * @return String describing current cache state
     */
    public String getCacheStats() {
        return String.format("Feedback: %d/%d | Searches: %d/%d",
                feedbackCache.size(), MAX_CACHE_SIZE,
                searchCache.size(), MAX_SEARCH_CACHE_SIZE);
    }
}
