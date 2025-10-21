package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.mapper.TradeLegMapper;
import com.technicalchallenge.model.*;
import com.technicalchallenge.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock private TradeRepository tradeRepository;
    @Mock private TradeLegRepository tradeLegRepository;
    @Mock private CashflowRepository cashflowRepository;
    @Mock private TradeStatusRepository tradeStatusRepository;
    @Mock private AdditionalInfoService additionalInfoService;
    @Mock private TradeLegMapper tradeLegMapper;
    @Mock private BookRepository bookRepository;
    @Mock private CounterpartyRepository counterpartyRepository;
    @Mock private ApplicationUserRepository applicationUserRepository;
    @Mock private TradeTypeRepository tradeTypeRepository;
    @Mock private TradeSubTypeRepository tradeSubTypeRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private LegTypeRepository legTypeRepository;
    @Mock private IndexRepository indexRepository;
    @Mock private HolidayCalendarRepository holidayCalendarRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private BusinessDayConventionRepository businessDayConventionRepository;
    @Mock private PayRecRepository payRecRepository;



    @InjectMocks private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;
    private TradeLeg mockTradeLeg;
    TradeStatus  tradeStatus;
    private Book mockBook;
    private Counterparty mockCounterparty;
    private Cashflow mockCashflow;



    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setBookId(100001L);
        tradeDTO.setBookName("TestBook");
        tradeDTO.setCounterpartyId(100001L);
        tradeDTO.setCounterpartyName("counterpartyName");
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        tradeStatus = new TradeStatus();
        mockCashflow = new Cashflow();

        mockTradeLeg = new TradeLeg();
        mockTradeLeg.setLegId(1L);
        mockTradeLeg.setNotional(BigDecimal.valueOf(1000000));
        mockTradeLeg.setRate(0.05);

        mockBook = new Book();
        mockBook.setBookName("TestBook");

        mockCounterparty = new Counterparty();
        mockCounterparty.setName("TestCounterparty");
        mockCounterparty.setId(1L);

        trade = new Trade();
        trade.setTradeId(100001L);
        trade.setVersion(1);
        trade.setActive(true);
        trade.setCounterparty(mockCounterparty);
        trade.setCreatedDate(LocalDateTime.now().minusDays(1));
        trade.setBook(mockBook);
        trade.setLastTouchTimestamp(LocalDateTime.now().minusDays(1));


        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));



    }

    @Test
    void testCreateTrade_Success() {
        // Given
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        when(bookRepository.findByBookName(anyString())).thenReturn(Optional.of(mockBook));
        when(counterpartyRepository.findByName(anyString())).thenReturn(Optional.of(mockCounterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(mockTradeLeg);


        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
        verify(bookRepository).findByBookName(anyString());
        verify(counterpartyRepository).findByName(anyString());
        verify(tradeStatusRepository).findByTradeStatus("NEW");
        verify(tradeLegRepository, times(2)).save(any(TradeLeg.class));


    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given - This test is intentionally failing for candidates to fix
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // This assertion is intentionally wrong - candidates need to fix it
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {

        // Given

        when(tradeRepository.findByTradeIdAndActiveTrue(100001L))
                .thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED"))
                .thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeRepository.save(any(Trade.class)))
                .thenReturn(trade);
        when(tradeLegRepository.save(any(TradeLeg.class)))
                .thenReturn(mockTradeLeg);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
        verify(tradeLegRepository, times(2)).save(any(TradeLeg.class));
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    // This test has a deliberate bug for candidates to find and fix
    @Test
    void testCashflowGeneration_MonthlySchedule() {
        // This test method is incomplete and has logical errors
        // Candidates need to implement proper cashflow testing
        // Given - setup is incomplete

        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        when(bookRepository.findByBookName(anyString())).thenReturn(Optional.of(mockBook));
        when(counterpartyRepository.findByName(anyString())).thenReturn(Optional.of(mockCounterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

        Schedule mockSchedule = new Schedule();
        mockSchedule.setSchedule("Monthly");

        // When - method call is missing
        when(tradeLegRepository.save(any(TradeLeg.class))).thenAnswer(invocation -> {
            TradeLeg savedLeg = invocation.getArgument(0);
            savedLeg.setLegId(1L);
            savedLeg.setCalculationPeriodSchedule(mockSchedule);
            return savedLeg;
        });

        // WHEN
        tradeService.createTrade(tradeDTO);

        // Then - assertions are wrong/missing
        // THEN
        // Two legs × 12 monthly cashflows = 24 saves
        verify(cashflowRepository, times(24)).save(any(Cashflow.class));
    }

}



