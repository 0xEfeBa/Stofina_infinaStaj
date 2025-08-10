package com.stofina.app.portfolioservice.controller;

import com.stofina.app.portfolioservice.dto.AccountDto;
import com.stofina.app.portfolioservice.request.account.*;
import com.stofina.app.portfolioservice.service.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.portfolioservice.constant.PortfolioConstants.*;

@Tag(
        name = "CRUD REST APIs for Account in Stofina",
        description = "REST APIs for managing accounts such as create, deposit, withdraw, and status update"
)
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(API_PREFIX + API_VERSION_V1 + API_ACCOUNTS)
public class AccountController {

    private final IAccountService accountService;

    @Operation(summary = "Create Account", description = "Creates a new trading account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get Account By ID", description = "Returns account details by account ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable("accountId") Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @Operation(summary = "Get Accounts by Customer ID", description = "Returns all accounts belonging to a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountDto>> getAccountsByCustomerId(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @Operation(summary = "Deposit Funds", description = "Deposits funds into the specified account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @PostMapping("/{accountId}/deposit")
    public void deposit(@PathVariable("accountId") Long accountId,
                                              @Valid @RequestBody DepositRequest request) {
        accountService.deposit(accountId, request);
    }

    @Operation(summary = "Withdraw Funds", description = "Withdraws funds from the specified account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdraw successful",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @PostMapping("/{accountId}/withdraw")
    public void withdraw(@PathVariable("accountId") Long accountId,
                                               @Valid @RequestBody WithdrawRequest request) {
        accountService.withdraw(accountId, request);
    }

    @Operation(summary = "Update Account Status", description = "Patches the status of an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = AccountDto.class)))
    })
    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(@PathVariable("accountId") Long accountId,
                                                          @Valid @RequestBody PatchAccountStatusRequest request) {
        return ResponseEntity.ok(accountService.updateStatus(accountId, request));
    }
    @Operation(summary = "Transfer Money", description = "Transfer Money to Another Account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/transfer-money")
    void transferMoney(TransferMoneyRequest request){
        accountService.transferMoney(request);
    }



}
