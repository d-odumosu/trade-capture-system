package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.SearchParameters;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
@Validated
@Tag(name = "Trades", description = "Trade management operations including booking, searching, and lifecycle management")
public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);


    private final TradeService tradeService;
    private final TradeMapper tradeMapper;

    public TradeController(TradeService tradeService, TradeMapper tradeMapper) {
        this.tradeService = tradeService;
        this.tradeMapper = tradeMapper;
    }

    @GetMapping
    @Operation(summary = "Get all trades",
            description = "Retrieves a list of all trades in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all trades",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TradeDTO> getAllTrades() {
        logger.info("Fetching all trades");
        return tradeService.getAllTrades().stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trade by ID",
            description = "Retrieves a specific trade by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade found and returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Trade not found"),
            @ApiResponse(responseCode = "400", description = "Invalid trade ID format")
    })
    public ResponseEntity<TradeDTO> getTradeById(
            @Parameter(description = "Unique identifier of the trade", required = true)
            @PathVariable(name = "id") Long id) {
        logger.debug("Fetching trade by id: {}", id);
        return tradeService.getTradeById(id)
                .map(tradeMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // Multi-criteria search start

    @GetMapping("/search")
    @Operation(
            summary = "Search trades (paginated, multi-criteria)",
            description = "Performs a multi-criteria search with pagination and sorting support."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trades retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TradeDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters or date range format"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving trades")
    })
    public ResponseEntity<Page<TradeDTO>> searchTrades(
            @RequestParam(value = "counterparty", required = false) String counterpartyName,
            @RequestParam(value = "book", required = false) String bookName,
            @RequestParam(value = "trader", required = false) String trader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            Pageable pageable // Spring creates this automatically
    ) {
        // Build SearchParameters using Lombok builder
        SearchParameters params = SearchParameters.builder()
                .counterpartyName(counterpartyName)
                .bookName(bookName)
                .trader(trader)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        Page<TradeDTO> results = tradeService.searchTradesPaginated(params, pageable);

        // Return paginated results
        return ResponseEntity.ok(results);
    }


    @PostMapping
    @Operation(summary = "Create new trade",
            description = "Creates a new trade with the provided details. Automatically generates cashflows and validates business rules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trade created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
            @ApiResponse(responseCode = "500", description = "Internal server error during trade creation")
    })
    public ResponseEntity<?> createTrade(
            @Parameter(description = "Trade details for creation", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {
        logger.info("Creating new trade: {}", tradeDTO);
        try {
            Trade trade = tradeMapper.toEntity(tradeDTO);
            tradeService.populateReferenceDataByName(trade, tradeDTO);
            Trade savedTrade = tradeService.saveTrade(trade, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(savedTrade);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating trade: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing trade",
            description = "Updates an existing trade with new information. Subject to business rule validation and user privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Trade not found"),
            @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to update trade")
    })
    public ResponseEntity<?> updateTrade(
            @Parameter(description = "Unique identifier of the trade to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated trade details", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {
        logger.info("Updating trade with id: {}", id);
        if (tradeDTO.getTradeId() == null || !tradeDTO.getTradeId().equals(id)) {
            return ResponseEntity.badRequest()
                    .body("Trade ID in path must match Trade ID in request body");
        }

        try {
            tradeDTO.setTradeId(id); // Ensure the ID matches
            Trade amendedTrade = tradeService.amendTrade(id, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(amendedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating trade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trade",
            description = "Deletes an existing trade. This is a soft delete that changes the trade status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trade not found"),
            @ApiResponse(responseCode = "400", description = "Trade cannot be deleted in current status"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to delete trade")
    })
    public ResponseEntity<?> deleteTrade(
            @Parameter(description = "Unique identifier of the trade to delete", required = true)
            @PathVariable Long id) {
        logger.info("Deleting trade with id: {}", id);
        try {
            tradeService.deleteTrade(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error deleting trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate trade",
            description = "Terminates an existing trade before its natural maturity date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade terminated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Trade not found"),
            @ApiResponse(responseCode = "400", description = "Trade cannot be terminated in current status"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to terminate trade")
    })
    public ResponseEntity<?> terminateTrade(
            @Parameter(description = "Unique identifier of the trade to terminate", required = true)
            @PathVariable Long id) {
        logger.info("Terminating trade with id: {}", id);
        try {
            Trade terminatedTrade = tradeService.terminateTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(terminatedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error terminating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error terminating trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel trade",
            description = "Cancels an existing trade by changing its status to cancelled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Trade not found"),
            @ApiResponse(responseCode = "400", description = "Trade cannot be cancelled in current status"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to cancel trade")
    })
    public ResponseEntity<?> cancelTrade(
            @Parameter(description = "Unique identifier of the trade to cancel", required = true)
            @PathVariable Long id) {
        logger.info("Cancelling trade with id: {}", id);
        try {
            Trade cancelledTrade = tradeService.cancelTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(cancelledTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error cancelling trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error cancelling trade: " + e.getMessage());
        }
    }


}
