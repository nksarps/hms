package com.nks.hms.service;

import com.nks.hms.model.Appointment;
import com.nks.hms.repository.IAppointmentRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caching layer for appointment data access with LRU eviction policy.
 * 
 * <p>Provides in-memory caching to reduce database load and improve response times
 * for frequently accessed appointments. Implements Least Recently Used (LRU) eviction
 * when cache reaches maximum capacity.
 * 
 * <p>Key features:
 * <ul>
 *   <li>ID-based caching using LinkedHashMap for O(1) access and LRU ordering</li>
 *   <li>Search result caching with configurable TTL (time-to-live)</li>
 *   <li>Automatic cache invalidation on create/update/delete operations</li>
 *   <li>In-memory sorting by appointment date</li>
 *   <li>Configurable cache size limits</li>
 * </ul>
 * 
 * <p>Thread safety: This implementation is NOT thread-safe. For multi-threaded
 * environments, external synchronization or ConcurrentHashMap should be used.
 * 
 * @see com.nks.hms.repository.AppointmentRepository
 * @see com.nks.hms.model.Appointment
 */
public class AppointmentCache {
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_SEARCH_CACHE_SIZE = 20;
    private static final long SEARCH_CACHE_TTL_MS = 60_000; // 1 minute
    
    private final IAppointmentRepository repository;
    
    /** LRU cache for individual appointments by ID */
    private final Map<Integer, Appointment> appointmentCache;
    
    /** Cache for search results with timestamp for TTL */
    private final Map<String, CachedSearchResult> searchCache;
    
    /**
     * Comparator options for sorting appointment lists.
     */
    public enum SortBy {
        DATE_ASC(Comparator.comparing(Appointment::getAppointmentDate)),
        DATE_DESC(Comparator.comparing(Appointment::getAppointmentDate).reversed()),
        TODAY(Comparator.comparing(Appointment::getAppointmentDate).reversed()),
        NEXT_7(Comparator.comparing(Appointment::getAppointmentDate).reversed()),
        NEXT_30(Comparator.comparing(Appointment::getAppointmentDate).reversed());
        
        private final Comparator<Appointment> comparator;
        
        SortBy(Comparator<Appointment> comparator) {
            this.comparator = comparator;
        }
        
        public Comparator<Appointment> getComparator() {
            return comparator;
        }
    }
    
    /**
     * Wrapper for cached search results with timestamp for TTL management.
     */
    private static class CachedSearchResult {
        final List<Appointment> results;
        final long timestamp;
        final int totalCount;
        
        CachedSearchResult(List<Appointment> results, int totalCount) {
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
     * @param repository The underlying appointment repository for database access
     */
    public AppointmentCache(IAppointmentRepository repository) {
        this.repository = repository;
        this.appointmentCache = new LinkedHashMap<Integer, Appointment>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Appointment> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        this.searchCache = new LinkedHashMap<String, CachedSearchResult>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedSearchResult> eldest) {
                return size() > MAX_SEARCH_CACHE_SIZE;
            }
        };
    }
    
    /**
     * Searches for appointments with caching and pagination support.
     * 
     * @param searchTerm Text to search for
     * @param limit Page size
     * @param offset Records to skip
     * @param sortBy Sort option
     * @return List of matching appointments
     * @throws SQLException If database access fails
     */
    public List<Appointment> find(String searchTerm, int limit, int offset, SortBy sortBy) throws SQLException {
        String key = searchTerm == null ? "" : searchTerm;
        String cacheKey = key + ":" + sortBy.name() + ":" + limit + ":" + offset;

        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return paginateAndSort(cached.results, sortBy, limit, offset);
        }

        boolean requiresFilter = sortBy == SortBy.TODAY || sortBy == SortBy.NEXT_7 || sortBy == SortBy.NEXT_30;
        List<Appointment> source;
        int totalCount;

        if (requiresFilter) {
            // Fetch a bounded set, apply filter client-side, then paginate
            source = repository.find(key, 2000, 0);
            source.forEach(appt -> appointmentCache.put(appt.getId(), appt));
            List<Appointment> filtered = applyDateFilter(source, sortBy);
            totalCount = filtered.size();
            searchCache.put(cacheKey, new CachedSearchResult(filtered, totalCount));
            return paginateAndSort(filtered, sortBy, limit, offset);
        }

        source = repository.find(key, limit, offset);
        source.forEach(appt -> appointmentCache.put(appt.getId(), appt));
        totalCount = repository.count(key);
        searchCache.put(cacheKey, new CachedSearchResult(source, totalCount));
        return source.stream()
                .sorted(sortBy.getComparator())
                .collect(Collectors.toList());
    }

    private List<Appointment> paginateAndSort(List<Appointment> list, SortBy sortBy, int limit, int offset) {
        return list.stream()
                .sorted(sortBy.getComparator())
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Appointment> applyDateFilter(List<Appointment> list, SortBy sortBy) {
        LocalDate today = LocalDate.now();
        LocalDate in7 = today.plusDays(7);
        LocalDate in30 = today.plusDays(30);

        return list.stream()
                .filter(appt -> {
                    LocalDate d = appt.getAppointmentDate().toLocalDate();
                    return switch (sortBy) {
                        case TODAY -> d.equals(today);
                        case NEXT_7 -> !d.isBefore(today) && !d.isAfter(in7);
                        case NEXT_30 -> !d.isBefore(today) && !d.isAfter(in30);
                        default -> true;
                    };
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Counts total appointments matching search criteria.
     * 
     * @param searchTerm Text to search for
     * @return Total count
     * @throws SQLException If database access fails
     */
    public int count(String searchTerm, SortBy sortBy) throws SQLException {
        String key = searchTerm == null ? "" : searchTerm;
        String cacheKey = key + ":count:" + sortBy.name();
        
        CachedSearchResult cached = searchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.totalCount;
        }

        boolean requiresFilter = sortBy == SortBy.TODAY || sortBy == SortBy.NEXT_7 || sortBy == SortBy.NEXT_30;
        if (requiresFilter) {
            List<Appointment> source = repository.find(key, 2000, 0);
            List<Appointment> filtered = applyDateFilter(source, sortBy);
            searchCache.put(cacheKey, new CachedSearchResult(filtered, filtered.size()));
            return filtered.size();
        }
        return repository.count(key);
    }
    
    /**
     * Retrieves an appointment by ID with caching.
     * 
     * @param id The appointment ID
     * @return Optional containing appointment if found
     * @throws SQLException If database access fails
     */
    public Optional<Appointment> findById(int id) throws SQLException {
        if (appointmentCache.containsKey(id)) {
            return Optional.of(appointmentCache.get(id));
        }
        
        Optional<Appointment> result = repository.findById(id);
        result.ifPresent(appt -> appointmentCache.put(appt.getId(), appt));
        return result;
    }
    
    /**
     * Retrieves all appointments for a patient.
     * 
     * @param patientId The patient's ID
     * @return List of appointments
     * @throws SQLException If database access fails
     */
    public List<Appointment> findByPatientId(int patientId) throws SQLException {
        List<Appointment> results = repository.findByPatientId(patientId);
        results.forEach(appt -> appointmentCache.put(appt.getId(), appt));
        return results;
    }
    
    /**
     * Retrieves all appointments for a doctor.
     * 
     * @param doctorId The doctor's ID
     * @return List of appointments
     * @throws SQLException If database access fails
     */
    public List<Appointment> findByDoctorId(int doctorId) throws SQLException {
        List<Appointment> results = repository.findByDoctorId(doctorId);
        results.forEach(appt -> appointmentCache.put(appt.getId(), appt));
        return results;
    }
    
    /**
     * Creates a new appointment and invalidates search cache.
     * 
     * @param appointment The appointment to create
     * @return Generated appointment ID
     * @throws SQLException If database operation fails
     */
    public int create(Appointment appointment) throws SQLException {
        int id = repository.create(appointment);
        appointment.setId(id);
        appointmentCache.put(id, appointment);
        searchCache.clear(); // Invalidate all search results
        return id;
    }
    
    /**
     * Updates an appointment and invalidates cache.
     * 
     * @param appointment The appointment with updated data
     * @throws SQLException If database operation fails
     */
    public void update(Appointment appointment) throws SQLException {
        repository.update(appointment);
        appointmentCache.put(appointment.getId(), appointment);
        searchCache.clear(); // Invalidate all search results
    }
    
    /**
     * Deletes an appointment and invalidates cache.
     * 
     * @param id The appointment ID
     * @throws SQLException If database operation fails
     */
    public void delete(int id) throws SQLException {
        repository.delete(id);
        appointmentCache.remove(id);
        searchCache.clear(); // Invalidate all search results
    }
    
    /**
     * Gets cache statistics for display purposes.
     * 
     * @return String with cache hit info
     */
    public String getStats() {
        return "Cached: " + appointmentCache.size() + " appointments";
    }
}
