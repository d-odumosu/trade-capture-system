package com.technicalchallenge.validation.refData;

import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link ReferenceDataGateway} that performs
 * database lookups via JPA repositories.
 *
 * <p>This class centralizes all reference data queries so that
 * validators never access repositories directly.</p>
 */
@Service
@AllArgsConstructor
public class ReferenceDataGatewayImpl implements ReferenceDataGateway {

    //  Repositories

    private final BookRepository bookRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ApplicationUserRepository userRepository;
    private final TradeTypeRepository tradeTypeRepository;
    private final TradeSubTypeRepository tradeSubTypeRepository;
    private final TradeStatusRepository tradeStatusRepository;

    //  BOOKS
    @Override
    public boolean bookExists(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public boolean isBookActive(Long id) {
        return bookRepository.findById(id)
                .map(Book::isActive)
                .orElse(false);
    }


    //  COUNTERPARTIES
    @Override
    public boolean counterpartyExists(Long id) {
        return counterpartyRepository.existsById(id);
    }

    @Override
    public boolean isCounterpartyActive(Long id) {
        return counterpartyRepository.findById(id)
                .map(Counterparty::isActive)
                .orElse(false);
    }


    //  USERS
    /**
     * Used for validating the logged-in user (based on loginId).
     */
    @Override
    public boolean userExists(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    /**
     * Used for validating trade references such as traderUserId or tradeInputterUserId.
     */
    @Override
    public boolean userExistsById(Long id) {
        return !userRepository.existsById(id);
    }

    @Override
    public boolean isUserActive(Long id) {
        return userRepository.findById(id)
                .map(ApplicationUser::isActive)
                .orElse(false);
    }


    //  TRADE METADATA
    @Override
    public boolean tradeTypeExists(Long id) {
        return tradeTypeRepository.existsById(id);
    }

    @Override
    public boolean tradeSubTypeExists(Long id) {
        return tradeSubTypeRepository.existsById(id);
    }

    @Override
    public boolean tradeStatusExists(Long id) {
        return tradeStatusRepository.existsById(id);
    }
}
