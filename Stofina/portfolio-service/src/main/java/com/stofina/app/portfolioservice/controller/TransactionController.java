package com.stofina.app.portfolioservice.controller;

import com.stofina.app.portfolioservice.dto.TransactionDto;
import com.stofina.app.portfolioservice.service.ITransactionService;
import com.stofina.app.portfolioservice.service.ITransactionService.TransactionSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.portfolioservice.constant.PortfolioConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(API_PREFIX + API_VERSION_V1 + API_TRANSACTION)
@Tag(name = "Transaction History API", description = "APIs for viewing transaction history, details, and summary")
public class TransactionController {

    private final ITransactionService transactionService;

    @Operation(summary = "Get transaction history by account ID (paged)",
            description = "Returns paged transaction history for the given account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history returned",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(
            @PathVariable("accountId") Long accountId
    ) {
        List<TransactionDto> history = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get transaction detail by ID",
            description = "Returns a transaction's full detail by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionDetail(
            @PathVariable("transactionId") Long transactionId
    ) {
        TransactionDto dto = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get summary of transactions by account ID",
            description = "Returns total BUY/SELL quantity and amount summary for an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction summary returned",
                    content = @Content(schema = @Schema(implementation = TransactionSummary.class)))
    })
    @GetMapping("/account/{accountId}/summary")
    public ResponseEntity<TransactionSummary> getTransactionSummary(
            @PathVariable("accountId") Long accountId
    ) {
        TransactionSummary summary = transactionService.getSummaryByAccount(accountId);
        return ResponseEntity.ok(summary);
    }
}
