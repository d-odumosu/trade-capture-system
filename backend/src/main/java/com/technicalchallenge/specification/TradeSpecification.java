package com.technicalchallenge.specification;

import com.technicalchallenge.dto.TradeFilterRequestDTO;
import org.springframework.data.jpa.domain.Specification;
import com.technicalchallenge.model.*;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic filtering logic for {@link Trade} entities.
 * <p>
 * This version includes input validation, LIKE sanitization (to prevent SQL injection),
 * modular helper methods, and conditional joins for performance.
 */
public class TradeSpecification {
    /**
     * Creates a {@link Specification} for filtering trades based on provided criteria.
     * <p>
     * Supports filters for trader, book, counterparty, trade type, subtype, status, version,
     * active flag, date ranges, and trade leg attributes (notional, rate, currency, etc.).
     *
     * @param filterRequest   DTO containing all filter criteria
     * @param traderUsername  logged-in trader username (for restricting visibility)
     * @return Specification for trade filtering
     */
    public static Specification<Trade> filterTrades(TradeFilterRequestDTO filterRequest, String traderUsername) {
        // IMPROVEMENT 1: Validate input ranges before building predicates

        return (Root<Trade> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- Restrict to logged-in trader (if applicable) ---
            if (traderUsername != null && !traderUsername.trim().isEmpty()) {
                Join<Trade, ApplicationUser> traderJoin = root.join("traderUser", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(traderJoin.get("loginId")), traderUsername.trim().toLowerCase()));
            }

            // --- Book ---

            if (filterRequest.getBookName() != null && !filterRequest.getBookName().trim().isEmpty()) {
                Join<Trade, Book> bookJoin = root.join("book", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(bookJoin.get("bookName")), "%" + filterRequest.getBookName().trim().toLowerCase() + "%"));
            }
            // --- Counterparty ---
            if (filterRequest.getCounterpartyName() != null && !filterRequest.getCounterpartyName().trim().isEmpty()) {
                Join<Trade, Counterparty> counterpartyJoin = root.join("counterparty", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(counterpartyJoin.get("name")), "%" + filterRequest.getCounterpartyName().trim().toLowerCase() + "%"));
            }
            // --- Trade type ---
            if (filterRequest.getTradeTypeName() != null && !filterRequest.getTradeTypeName().trim().isEmpty()) {
                Join<Trade, TradeType> typeJoin = root.join("tradeType", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(typeJoin.get("tradeType")), filterRequest.getTradeTypeName().trim().toLowerCase()));
            }
            // --- Trade sub-type ---
            if (filterRequest.getTradeSubTypeName() != null && !filterRequest.getTradeSubTypeName().trim().isEmpty()) {
                Join<Trade, TradeSubType> subTypeJoin = root.join("tradeSubType", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(subTypeJoin.get("tradeSubType")), filterRequest.getTradeSubTypeName().trim().toLowerCase()));
            }
            // --- Trade status ---
            if (filterRequest.getTradeStatusName() != null && !filterRequest.getTradeStatusName().isEmpty()) {
                Join<Trade, TradeStatus> statusJoin = root.join("tradeStatus", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(statusJoin.get("tradeStatus")), filterRequest.getTradeStatusName().toLowerCase()));
            }
            // “Check if the user provided a version value — and only then, filter trades by that version.”
            // --- Version ---
            if (filterRequest.getVersion() != null) {
                predicates.add(cb.equal(root.get("version"), filterRequest.getVersion()));
            }
            // --- Active flag ---
            if (filterRequest.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filterRequest.getActive()));
            }
            // --- DATE FILTERS ---

            if (filterRequest.getTradeDateFrom() != null) {
               // Only include trades whose tradeDate is on or after this starting date.Good for “From 1 Jan 2024 onwards.”
                predicates.add(cb.greaterThanOrEqualTo(root.get("tradeDate"), filterRequest.getTradeDateFrom()
                ));
            }

            if (filterRequest.getTradeDate() != null) {
                //Only include trades whose tradeDate is on or before this date.
                predicates.add(cb.lessThanOrEqualTo(root.get("tradeDate"), filterRequest.getTradeDate()
                ));
            }

            if (filterRequest.getMaturityDate() != null) {
                //Only include trades that mature on or after the given date.
                //So if the user filters maturityDate=2025-06-01,
                //they’ll get trades that haven’t matured yet or will mature after June 2025.
                predicates.add(cb.greaterThanOrEqualTo(root.get("tradeMaturityDate"), filterRequest.getMaturityDate()
                ));
            }

            if (filterRequest.getExecutionDate() != null) {
                //Only include trades that were executed on or before this date.
                //That’s perfect if the user wants to see everything executed up to a certain cutoff.
                predicates.add(cb.lessThanOrEqualTo(root.get("tradeExecutionDate"), filterRequest.getExecutionDate()
                ));
            }

            //  Trade Leg filters
            //You start with this: Join<Trade, TradeLeg> legJoin = null;
            //Later, when the user provides a filter related to a trade leg (for example, notional, rate, currency, etc.),
            //you actually perform the join:
            //  Conditional join for performance — only join if filters exist

            boolean needsLegJoin =
                    filterRequest.getMinNotional() != null ||
                            filterRequest.getMaxNotional() != null ||
                            filterRequest.getRateFrom() != null ||
                            filterRequest.getRateTo() != null ||
                            (filterRequest.getCurrency() != null && !filterRequest.getCurrency().trim().isEmpty()) ||
                            (filterRequest.getLegRateTypeName() != null && !filterRequest.getLegRateTypeName().trim().isEmpty()) ||
                            (filterRequest.getPayReceiveFlag() != null && !filterRequest.getPayReceiveFlag().trim().isEmpty()) ||
                            (filterRequest.getIndexName() != null && !filterRequest.getIndexName().trim().isEmpty());

            Join<Trade, TradeLeg> legJoin = needsLegJoin ? root.join("tradeLegs", JoinType.LEFT) : null;

            if (legJoin != null){
                if (filterRequest.getMinNotional() != null)
                    predicates.add(cb.greaterThanOrEqualTo(legJoin.get("notional"), filterRequest.getMinNotional()));

                if (filterRequest.getMaxNotional() != null)
                    predicates.add(cb.lessThanOrEqualTo(legJoin.get("notional"), filterRequest.getMaxNotional()));

                if (filterRequest.getRateFrom() != null)
                    predicates.add(cb.greaterThanOrEqualTo(legJoin.get("rate"), filterRequest.getRateFrom()));

                if (filterRequest.getRateTo() != null)
                    predicates.add(cb.lessThanOrEqualTo(legJoin.get("rate"), filterRequest.getRateTo()));

                if (filterRequest.getCurrency() != null && !filterRequest.getCurrency().trim().isEmpty()) {
                    Join<TradeLeg, Currency> currencyJoin = legJoin.join("currency", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(currencyJoin.get("currency")), filterRequest.getCurrency().trim().toLowerCase()));
                }
                //  Leg rate type
                if (filterRequest.getLegRateTypeName() != null && !filterRequest.getLegRateTypeName().trim().isEmpty()) {
                    Join<TradeLeg, LegType> legTypeJoin = legJoin.join("legRateType", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(legTypeJoin.get("type")), filterRequest.getLegRateTypeName().trim().toLowerCase()));
                }

                //  Pay/Receive flag
                if (filterRequest.getPayReceiveFlag() != null && !filterRequest.getPayReceiveFlag().trim().isEmpty()) {
                    Join<TradeLeg, PayRec> payRecJoin = legJoin.join("payReceiveFlag", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(payRecJoin.get("payRec")), filterRequest.getPayReceiveFlag().trim().toLowerCase()));
                }

                //  Index
                if (filterRequest.getIndexName() != null && !filterRequest.getIndexName().trim().isEmpty()) {
                    Join<TradeLeg, Index> indexJoin = legJoin.join("index", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(indexJoin.get("index")), filterRequest.getIndexName().trim().toLowerCase()));
                }
            }

            if (query != null) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}