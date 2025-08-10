package com.stofina.app.portfolioservice.controller;

import com.stofina.app.portfolioservice.dto.BalanceDto;
import com.stofina.app.portfolioservice.dto.WithdrawableBalanceDto;
import com.stofina.app.portfolioservice.dto.WithdrawalRestrictionDto;
import com.stofina.app.portfolioservice.dto.BalanceReservationDto;
import com.stofina.app.portfolioservice.service.IAccountService;
import com.stofina.app.portfolioservice.service.IWithdrawalRestrictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.portfolioservice.constant.PortfolioConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(API_PREFIX + API_VERSION_V1 + API_BALANCES)
@Tag(name = "Balance API", description = "APIs to query detailed, withdrawable, and reserved balances for accounts")
public class BalanceController {

    private final IAccountService accountService;
    private final IWithdrawalRestrictionService restrictionService;

    @Operation(summary = "Get full balance info", description = "Returns detailed balance information for given account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned",
                    content = @Content(schema = @Schema(implementation = BalanceDto.class)))
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<BalanceDto> getAccountBalance(
            @PathVariable("accountId") Long accountId
    ) {
        return ResponseEntity.ok(accountService.getBalanceByAccountId(accountId));
    }

    @Operation(summary = "Get withdrawable balance", description = "Returns the amount that can be withdrawn now (T+2 applied)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawable balance returned",
                    content = @Content(schema = @Schema(implementation = WithdrawableBalanceDto.class)))
    })
    @GetMapping("/account/{accountId}/withdrawable")
    public ResponseEntity<WithdrawableBalanceDto> getWithdrawableBalance(
            @PathVariable("accountId") Long accountId
    ) {
        return ResponseEntity.ok(accountService.getWithdrawableBalance(accountId));
    }

    @Operation(summary = "Get T+2 withdrawal restrictions", description = "Returns all active withdrawal restrictions for account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restrictions returned",
                    content = @Content(schema = @Schema(implementation = WithdrawalRestrictionDto.class)))
    })
    @GetMapping("/account/{accountId}/restrictions")
    public ResponseEntity<List<WithdrawalRestrictionDto>> getWithdrawalRestrictions(
            @PathVariable("accountId") Long accountId
    ) {
        return ResponseEntity.ok(restrictionService.getRestrictionsByAccountId(accountId));
    }

    @Operation(summary = "Get reserved balances", description = "Returns all active reservations (locked funds for pending buy orders)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations returned",
                    content = @Content(schema = @Schema(implementation = BalanceReservationDto.class)))
    })
    @GetMapping("/account/{accountId}/reservations")
    public ResponseEntity<List<BalanceReservationDto>> getBalanceReservations(
            @PathVariable("accountId") Long accountId
    ) {
        return ResponseEntity.ok(accountService.getActiveReservationsByAccountId(accountId));
    }
}
