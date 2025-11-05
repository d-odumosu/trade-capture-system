package com.technicalchallenge.validation.validator;

import com.technicalchallenge.validation.ValidationContext;
import com.technicalchallenge.validation.ValidationResult;

/**
 * Common contract for all validation components.
 *
 * <p>Every validator checks a specific set of business rules (dates, privileges, etc.)
 * and records any errors or warnings into the shared {@link ValidationResult}.
 */
public interface Validator {
    /**
     * Performs validation on the given context and appends any findings to the result.
     *
     * @param context  provides access to trade, user, and reference data
     * @param result   shared container for validation messages
     */
    void validate(ValidationContext context, ValidationResult result );
}
