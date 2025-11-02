package com.technicalchallenge.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for advanced trade filtering.
 * Used by the /filter endpoint for traders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeFilterRequestDTO {

    // TRADE-LEVEL FILTERS

    /** Book name (e.g. "FX_LDN_BOOK") */
    private String bookName;

    /** Counterparty name (e.g. "BigBank") */
    private String counterpartyName;

    /** Current  logged in trader username/login ID (usually filled from auth context) */
    private String traderLoginId;

    /** Trade type (e.g. "FX", "SWAP", "LOAN") */
    private String tradeTypeName;

    /** Trade sub-type (e.g. "FX SPOT", "FX FORWARD") */
    private String tradeSubTypeName;

    /** Trade status (e.g. "APPROVED", "PENDING") */
    private String tradeStatusName;

    /** Version number */
    private Integer version;

    /** Whether the trade is active */
    private Boolean active;

    //  DATE FILTERS
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tradeDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tradeDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maturityDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate executionDate;

    //  TRADE-LEG FILTERS
    private BigDecimal minNotional;
    private BigDecimal maxNotional;

    private Double rateFrom;
    private Double rateTo;

    /** Currency (e.g. "USD", "GBP") */
    private String currency;

    /** Leg type (e.g. "FIXED", "FLOATING") */
    private String legRateTypeName;

    /** Pay/Receive flag */
    private String payReceiveFlag;

    /** Reference index (e.g. "LIBOR", "SONIA") */
    private String indexName;
}
