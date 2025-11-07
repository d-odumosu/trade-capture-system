package com.technicalchallenge.validation.refData;

/**
 * Provides a unified gateway for looking up reference data used in validation.
 *
 * <p>This interface allows validators to check whether related entities
 * (Book, Counterparty, User, TradeType, etc.) exist and are active in the database,
 * without directly accessing repositories.</p>
 *
 * <p>Each method represents a specific type of lookup that validators
 * can call through the {@code ReferenceDataGatewayImpl} implementation.</p>
 */
public interface ReferenceDataGateway {

    // BOOKS

    /**
     * Checks whether a Book exists by its ID.
     *
     * @param id the Book ID
     * @return true if the book exists, false otherwise
     */
    boolean bookExists(Long id);

    /**
     * Checks whether a Book is active.
     *
     * @param id the Book ID
     * @return true if the book is active, false otherwise
     */
    boolean isBookActive(Long id);


    //  COUNTERPARTIES


    /**
     * Checks whether a Counterparty exists by its ID.
     *
     * @param id the Counterparty ID
     * @return true if the counterparty exists, false otherwise
     */
    boolean counterpartyExists(Long id);

    /**
     * Checks whether a Counterparty is active.
     *
     * @param id the Counterparty ID
     * @return true if the counterparty is active, false otherwise
     */
    boolean isCounterpartyActive(Long id);


    //  USERS

    /**
     * Checks whether a User exists by their loginId (for logged-in user validation).
     *
     * @param loginId the user’s unique login identifier
     * @return true if the user exists, false otherwise
     */
    boolean userExists(String loginId);

    /**
     * Checks whether a User exists by their numeric ID (for trade references such as trader or inputter).
     *
     * @param id the user’s numeric ID
     * @return true if the user exists, false otherwise
     */
    boolean userExistsById(Long id);

    /**
     * Checks whether a User is active.
     *
     * @param id the user’s numeric ID
     * @return true if the user is active, false otherwise
     */
    boolean isUserActive(Long id);


    //  TRADE METADATA


    /**
     * Checks whether a Trade Type exists.
     *
     * @param id the trade type ID
     * @return true if it exists, false otherwise
     */
    boolean tradeTypeExists(Long id);

    /**
     * Checks whether a Trade Sub-Type exists.
     *
     * @param id the trade sub-type ID
     * @return true if it exists, false otherwise
     */
    boolean tradeSubTypeExists(Long id);

    /**
     * Checks whether a Trade Status exists.
     *
     * @param id the trade status ID
     * @return true if it exists, false otherwise
     */
    boolean tradeStatusExists(Long id);
}
