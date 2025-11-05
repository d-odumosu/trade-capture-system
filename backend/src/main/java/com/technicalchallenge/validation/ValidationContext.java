package com.technicalchallenge.validation;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.validation.refData.ReferenceDataGateway;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Provides contextual information required during trade validation.
 *
 * <p>This class bundles all relevant data that validators might need,
 * so they can focus purely on business rules rather than data retrieval.</p>
 *
 *
 * <p>Each validator receives the same {@code ValidationContext}
 * and can use its fields as needed. This ensures consistent access
 * to trade, user, and system state across all validation layers.</p>
 */
@Getter
@AllArgsConstructor
@ToString
public class ValidationContext {

    /** The trade being validated. */
    private final TradeDTO trade;

    /** The authenticated user performing the validation or operation. */
    private final ApplicationUser user;

    /** Provides access to reference data and database lookups. */
    private final ReferenceDataGateway refDataGateway;

    /** The current system date used in date-based validations. */
    private final LocalDate currentDate;

    /** The operation type for which this validation is being executed. */
    private final TradeOperationType operation;

    /**
     * Enumerates supported trade operations for rule scoping.
     * Validators can use this to determine which checks apply.
     */
    public enum TradeOperationType {
        CREATE,
        AMEND,
        CANCEL,
        TERMINATE,
        VIEW
    }
}
