package com.stofina.app.portfolioservice.controller;

import com.stofina.app.portfolioservice.dto.StockDto;
import com.stofina.app.portfolioservice.request.account.TransferStockRequest;
import com.stofina.app.portfolioservice.request.stock.BuyStockRequest;
import com.stofina.app.portfolioservice.request.stock.SellStockRequest;
import com.stofina.app.portfolioservice.service.IStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.portfolioservice.constant.PortfolioConstants.*;

@Tag(
        name = "CRUD REST APIs for Stock in Stofina",
        description = "REST APIs in Stofina to BUY, SELL, LIST and DELETE stock holdings"
)
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@RequestMapping(API_PREFIX + API_VERSION_V1 + API_STOCK)
public class StockController {

    private final IStockService stockService;

    @Operation(summary = "Buy Stock", description = "Places a buy order and reserves funds")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy order placed",
                    content = @Content(schema = @Schema(implementation = StockDto.class)))
    })
    @PostMapping("/buy")
    public void buyStock(@Valid @RequestBody BuyStockRequest request) {
       stockService.buyStock( request);
    }

    @Operation(summary = "Sell Stock", description = "Places a sell order and applies T+2 restriction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sell order placed",
                    content = @Content(schema = @Schema(implementation = StockDto.class)))
    })
    @PostMapping("/sell")
    public void sellStock(@Valid @RequestBody SellStockRequest request) {
        stockService.sellStock( request);
    }

    @Operation(summary = "Get Stocks by Account", description = "Returns all stock holdings for a given account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stocks found",
                    content = @Content(schema = @Schema(implementation = StockDto.class)))
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<List<StockDto>> getStocksByAccount(@PathVariable("accountId") Long accountId) {
        return ResponseEntity.ok(stockService.getStocksByAccountId(accountId));
    }

    @Operation(summary = "Get Stock by Account and Symbol", description = "Returns specific stock holding by account and symbol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock found",
                    content = @Content(schema = @Schema(implementation = StockDto.class)))
    })
    @GetMapping("/{accountId}/symbol/{symbol}")
    public ResponseEntity<StockDto> getStockByAccountAndSymbol(@PathVariable("accountId") Long accountId,
                                                               @PathVariable("symbol") String symbol) {
        return ResponseEntity.ok(stockService.getStockByAccountIdAndSymbol(accountId, symbol));
    }
    @Operation(summary = "Confirm Buy Order", description = "Confirms a buy order and finalizes the transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy order confirmed")
    })
    @PostMapping("/confirm-buy/{orderId}")
    public ResponseEntity<Void> confirmBuy(@PathVariable("orderId") Long orderId) {
        stockService.confirmBuy(orderId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Confirm Buy Order Partially", description = "Confirms a buy order partially and finalizes fulfilled portion")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partial buy order confirmed")
    })
    @PostMapping("/confirm-buy-partial/{orderId}/{fulfilledQuantity}")
    public ResponseEntity<Void> confirmBuyPartially(@PathVariable ("orderId")  Long orderId,
                                                    @PathVariable ("fulfilledQuantity")  int fulfilledQuantity) {
        stockService.confirmBuyPartially(orderId, fulfilledQuantity);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Cancel Buy Order", description = "Cancels a buy order and refunds reserved funds")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy order cancelled")
    })
    @DeleteMapping("/cancel-buy/{orderId}")
    public ResponseEntity<Void> cancelBuy(@PathVariable ("orderId")  Long orderId) {
        stockService.cancelBuy(orderId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Confirm Sell Order", description = "Confirms a sell order and finalizes the transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sell order confirmed")
    })
    @PostMapping("/confirm-sell/{orderId}")
    public ResponseEntity<Void> confirmSell(@PathVariable ("orderId")  Long orderId) {
        stockService.confirmSell(orderId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Confirm Sell Order Partially", description = "Confirms a sell order partially and finalizes fulfilled portion")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partial sell order confirmed")
    })
    @PostMapping("/confirm-sell-partial/{orderId}/{fulfilledQuantity}")
    public ResponseEntity<Void> confirmSellPartially(@PathVariable  ("orderId")  Long orderId,
                                                     @PathVariable ("fulfilledQuantity") int fulfilledQuantity) {
        stockService.confirmSellPartially(orderId, fulfilledQuantity);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Cancel Sell Order", description = "Cancels a sell order and releases reserved stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sell order cancelled")
    })
    @DeleteMapping("/cancel-sell/{orderId}")
    public ResponseEntity<Void> cancelSell(@PathVariable ("orderId")  Long orderId) {
        stockService.cancelSell(orderId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Transfer Stock", description = "Transfers a stock from one account to another")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock transferred successfully")
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferStock(@Valid @RequestBody TransferStockRequest request) {
        stockService.transferStock(request);
        return ResponseEntity.ok().build();
    }

}
