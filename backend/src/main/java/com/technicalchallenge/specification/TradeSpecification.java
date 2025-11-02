package com.technicalchallenge.specification;

import com.technicalchallenge.dto.TradeFilterRequestDTO;
import org.springframework.data.jpa.domain.Specification;
import com.technicalchallenge.model.*;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class TradeSpecification {
    public static Specification<Trade> filterTrades(TradeFilterRequestDTO filterRequest, String traderUsername) {
        return (Root<Trade> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- Restrict to logged-in trader (if applicable) ---
            // traderUsername stores the logged-in user
            if (traderUsername != null && !traderUsername.isEmpty()) {
                Join<Trade, ApplicationUser> traderJoin = root.join("traderUser", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(traderJoin.get("loginId")), traderUsername.toLowerCase()));
            }

            // --- Book ---

            if (filterRequest.getBookName() != null && !filterRequest.getBookName().isEmpty()) {
                Join<Trade, Book> bookJoin = root.join("book", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(bookJoin.get("bookName")), "%" + filterRequest.getBookName() + "%"));
            }
            // --- COUNTERPARTY ---
            if (filterRequest.getCounterpartyName() != null && !filterRequest.getCounterpartyName().isEmpty()) {
                Join<Trade, Counterparty> counterpartyJoin = root.join("counterparty", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(counterpartyJoin.get("name")), "%" + filterRequest.getCounterpartyName() + "%"));
            }
            // --- Trade type ---
            if (filterRequest.getTradeTypeName() != null && !filterRequest.getTradeTypeName().isEmpty()) {
                Join<Trade, TradeType> typeJoin = root.join("tradeType", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(typeJoin.get("tradeType")), filterRequest.getTradeTypeName().toLowerCase()));
            }
            // --- Trade sub-type ---
            if (filterRequest.getTradeSubTypeName() != null && !filterRequest.getTradeSubTypeName().isEmpty()) {
                Join<Trade, TradeSubType> subTypeJoin = root.join("tradeSubType", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(subTypeJoin.get("tradeSubType")), filterRequest.getTradeSubTypeName().toLowerCase()));
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

            // --- Trade Leg filters ---
            //You start with this: Join<Trade, TradeLeg> legJoin = null;
            //Later, when the user provides a filter related to a trade leg (for example, notional, rate, currency, etc.),
            //you actually perform the join:
            Join<Trade, TradeLeg> legJoin = null;
            boolean needsLegJoin =
                    //This block decides whether we should create the join:
                    filterRequest.getMinNotional() != null || filterRequest.getMaxNotional() != null ||
                            filterRequest.getRateFrom() != null || filterRequest.getRateTo() != null ||
                            filterRequest.getCurrency() != null || filterRequest.getLegRateTypeName() != null ||
                            filterRequest.getPayReceiveFlag() != null || filterRequest.getIndexName() != null;

            if (needsLegJoin) legJoin = root.join("tradeLegs", JoinType.LEFT);
            if (legJoin != null){
                //legJoin starts as null. It only gets a real value if needsLegJoin is true (meaning the user actually asked for any leg filters).
                // If needsLegJoin is false, that means the user didn’t filter by leg data, and we never joined TradeLeg.
                if (filterRequest.getMinNotional() != null)
                    predicates.add(cb.greaterThanOrEqualTo(legJoin.get("notional"), filterRequest.getMinNotional()));

                if (filterRequest.getMaxNotional() != null)
                    predicates.add(cb.lessThanOrEqualTo(legJoin.get("notional"), filterRequest.getMaxNotional()));

                if (filterRequest.getRateFrom() != null)
                    predicates.add(cb.greaterThanOrEqualTo(legJoin.get("rate"), filterRequest.getRateFrom()));

                if (filterRequest.getRateTo() != null)
                    predicates.add(cb.lessThanOrEqualTo(legJoin.get("rate"), filterRequest.getRateTo()));

                if (filterRequest.getCurrency() != null && !filterRequest.getCurrency().isEmpty()) {
                    Join<TradeLeg, Currency> currencyJoin = legJoin.join("currency", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(currencyJoin.get("currency")), filterRequest.getCurrency().toLowerCase()));
                }
                if (filterRequest.getLegRateTypeName() != null && !filterRequest.getLegRateTypeName().isEmpty()) {
                    Join<TradeLeg, LegType> legTypeJoin = legJoin.join("legRateType", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(legTypeJoin.get("type")), filterRequest.getLegRateTypeName().toLowerCase()));
                }

                if (filterRequest.getPayReceiveFlag() != null && !filterRequest.getPayReceiveFlag().isEmpty()) {
                    Join<TradeLeg, PayRec> payRecJoin = legJoin.join("payReceiveFlag", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(payRecJoin.get("payRec")), filterRequest.getPayReceiveFlag().toLowerCase()));
                }

                if (filterRequest.getIndexName() != null && !filterRequest.getIndexName().isEmpty()) {
                    Join<TradeLeg, Index> indexJoin = legJoin.join("index", JoinType.LEFT);
                    predicates.add(cb.equal(cb.lower(indexJoin.get("index")), filterRequest.getIndexName().toLowerCase()));
                }

            }

            if (query != null) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}