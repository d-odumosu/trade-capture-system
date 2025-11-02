package com.technicalchallenge.repository;

import com.technicalchallenge.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {
    // Existing methods
    List<Trade> findByTradeId(Long tradeId);

    @Query("SELECT MAX(t.tradeId) FROM Trade t")
    Optional<Long> findMaxTradeId();

    @Query("SELECT MAX(t.version) FROM Trade t WHERE t.tradeId = :tradeId")
    Optional<Integer> findMaxVersionByTradeId(@Param("tradeId") Long tradeId);

    // NEW METHODS for service layer compatibility
    Optional<Trade> findByTradeIdAndActiveTrue(Long tradeId);

    List<Trade> findByActiveTrueOrderByTradeIdDesc();

    @Query("""
    SELECT t FROM Trade t
    WHERE (:counterpartyName IS NULL OR LOWER(t.counterparty.name) LIKE LOWER(CONCAT('%', :counterpartyName, '%')))
      AND (:bookName IS NULL OR LOWER(t.book.bookName) LIKE LOWER(CONCAT('%', :bookName, '%')))
      AND (:trader IS NULL OR LOWER(CONCAT(t.traderUser.firstName, ' ', t.traderUser.lastName)) LIKE LOWER(CONCAT('%', :trader, '%'))
           OR LOWER(t.traderUser.loginId) LIKE LOWER(CONCAT('%', :trader, '%')))
      AND (:status IS NULL OR LOWER(t.tradeStatus.tradeStatus) = LOWER(:status))
      AND (:fromDate IS NULL OR t.tradeDate >= :fromDate)
      AND (:toDate IS NULL OR t.tradeDate <= :toDate)
""")
    Page<Trade> findBySearchCriteria(
            @Param("counterpartyName") String counterpartyName,
            @Param("bookName") String bookName,
            @Param("trader") String trader,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

}

