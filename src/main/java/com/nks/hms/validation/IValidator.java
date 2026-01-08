package com.nks.hms.validation;

import java.util.Optional;

/**
 * Generic validator interface for entity validation.
 * Promotes interface segregation by separating validation concerns.
 * 
 * @param <T> The entity type to validate
 */
public interface IValidator<T> {
    /**
     * Validates an entity and returns an error message if invalid.
     * 
     * @param entity The entity to validate
     * @return Optional containing error message if validation fails, empty if valid
     */
    Optional<String> validate(T entity);
}
