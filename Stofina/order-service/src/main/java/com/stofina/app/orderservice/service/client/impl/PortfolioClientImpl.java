package com.stofina.app.orderservice.service.client.impl;

import com.stofina.app.orderservice.dto.portfolio.*;
import com.stofina.app.orderservice.exception.portfolio.*;
import com.stofina.app.orderservice.service.client.PortfolioClient;
// Circuit Breaker imports removed - not needed
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioClientImpl implements PortfolioClient {

    @Qualifier("portfolioWebClient")
    private final WebClient portfolioWebClient;

    @Value("${portfolio-service.endpoints.buy-stock}")
    private String buyStockEndpoint;

    @Value("${portfolio-service.endpoints.sell-stock}")
    private String sellStockEndpoint;

    @Value("${portfolio-service.endpoints.confirm-buy}")
    private String confirmBuyEndpoint;

    @Value("${portfolio-service.endpoints.confirm-sell}")
    private String confirmSellEndpoint;

    @Value("${portfolio-service.endpoints.confirm-buy-partial}")
    private String confirmBuyPartialEndpoint;

    @Value("${portfolio-service.endpoints.confirm-sell-partial}")
    private String confirmSellPartialEndpoint;

    @Value("${portfolio-service.endpoints.cancel-buy}")
    private String cancelBuyEndpoint;

    @Value("${portfolio-service.endpoints.cancel-sell}")
    private String cancelSellEndpoint;

    @Value("${portfolio-service.endpoints.health}")
    private String healthEndpoint;

    // PHASE 1: BASIC RESERVATIONS

    @Override
    public CompletableFuture<PortfolioResponse> reserveBuyStock(BuyStockRequest request) {
        log.info("Reserving buy stock → AccountId: {}, Symbol: {}, Quantity: {}, Price: {}", 
                request.getAccountId(), request.getSymbol(), request.getQuantity(), request.getPrice());

        return portfolioWebClient.post()
                .uri(buyStockEndpoint)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    // Portfolio Service returns empty body with 200 OK = success
                    log.info("Buy stock reservation successful → OrderId: {}, HTTP Status: {}", 
                            request.getOrderId(), response.getStatusCode());
                    return PortfolioResponse.success("Stock purchase reservation successful", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> reserveSellStock(SellStockRequest request) {
        log.info("Reserving sell stock → AccountId: {}, Symbol: {}, Quantity: {}", 
                request.getAccountId(), request.getSymbol(), request.getQuantity());

        return portfolioWebClient.post()
                .uri(sellStockEndpoint)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    // Portfolio Service returns empty body with 200 OK = success
                    log.info("Sell stock reservation successful → OrderId: {}, HTTP Status: {}", 
                            request.getOrderId(), response.getStatusCode());
                    return PortfolioResponse.success("Stock sell reservation successful", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    // PHASE 2: TRADE CONFIRMATIONS

    @Override
    public CompletableFuture<PortfolioResponse> confirmBuyTrade(TradeConfirmationRequest request) {
        log.info("Confirming buy trade → TradeId: {}, OrderId: {}, ExecutedQuantity: {}", 
                request.getTradeId(), request.getOrderId(), request.getExecutedQuantity());

        // Portfolio Service expects path parameter: /confirm-buy/{orderId}
        String uri = confirmBuyEndpoint + "/" + request.getOrderId();

        return portfolioWebClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Buy trade confirmation successful → TradeId: {}, HTTP Status: {}", 
                            request.getTradeId(), response.getStatusCode());
                    return PortfolioResponse.success("Buy trade confirmed", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> confirmSellTrade(TradeConfirmationRequest request) {
        log.info("Confirming sell trade → TradeId: {}, OrderId: {}, ExecutedQuantity: {}", 
                request.getTradeId(), request.getOrderId(), request.getExecutedQuantity());

        // Portfolio Service expects path parameter: /confirm-sell/{orderId}
        String uri = confirmSellEndpoint + "/" + request.getOrderId();

        return portfolioWebClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Sell trade confirmation successful → TradeId: {}, HTTP Status: {}", 
                            request.getTradeId(), response.getStatusCode());
                    return PortfolioResponse.success("Sell trade confirmed", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> confirmPartialBuyTrade(PartialTradeConfirmationRequest request) {
        log.info("Confirming partial buy trade → TradeId: {}, OrderId: {}, PartialQuantity: {}, RemainingQuantity: {}", 
                request.getTradeId(), request.getOrderId(), request.getPartialQuantity(), request.getRemainingQuantity());

        // Portfolio Service expects path parameters: /confirm-buy-partial/{orderId}/{fulfilledQuantity}
        String uri = confirmBuyPartialEndpoint + "/" + request.getOrderId() + "/" + request.getPartialQuantity();

        return portfolioWebClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Partial buy trade confirmation successful → TradeId: {}, HTTP Status: {}", 
                            request.getTradeId(), response.getStatusCode());
                    return PortfolioResponse.success("Partial buy trade confirmed", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> confirmPartialSellTrade(PartialTradeConfirmationRequest request) {
        log.info("Confirming partial sell trade → TradeId: {}, OrderId: {}, PartialQuantity: {}, RemainingQuantity: {}", 
                request.getTradeId(), request.getOrderId(), request.getPartialQuantity(), request.getRemainingQuantity());

        // Portfolio Service expects path parameters: /confirm-sell-partial/{orderId}/{fulfilledQuantity}
        String uri = confirmSellPartialEndpoint + "/" + request.getOrderId() + "/" + request.getPartialQuantity();

        return portfolioWebClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Partial sell trade confirmation successful → TradeId: {}, HTTP Status: {}", 
                            request.getTradeId(), response.getStatusCode());
                    return PortfolioResponse.success("Partial sell trade confirmed", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    // PHASE 3: ORDER CANCELLATIONS

    @Override
    public CompletableFuture<PortfolioResponse> cancelBuyOrder(OrderCancellationRequest request) {
        log.info("Cancelling buy order → OrderId: {}, Reason: {}", request.getOrderId(), request.getReason());

        // Portfolio Service uses DELETE method with orderId in path
        String uri = cancelBuyEndpoint.replace("{orderId}", request.getOrderId().toString());
        
        return portfolioWebClient.delete()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Buy order cancellation successful → OrderId: {}, HTTP Status: {}", 
                            request.getOrderId(), response.getStatusCode());
                    return PortfolioResponse.success("Buy order cancelled", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> cancelSellOrder(OrderCancellationRequest request) {
        log.info("Cancelling sell order → OrderId: {}, Reason: {}", request.getOrderId(), request.getReason());

        // Portfolio Service uses DELETE method with orderId in path
        String uri = cancelSellEndpoint.replace("{orderId}", request.getOrderId().toString());
        
        return portfolioWebClient.delete()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Sell order cancellation successful → OrderId: {}, HTTP Status: {}", 
                            request.getOrderId(), response.getStatusCode());
                    return PortfolioResponse.success("Sell order cancelled", null);
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    // PHASE 4: ADVANCED COMPENSATION OPERATIONS (TODO: Will be implemented in Phase 3)

    @Override
    public CompletableFuture<PortfolioResponse> compensateTrade(CompensationRequest request) {
        log.info("Starting trade compensation → Type: {}, OrderId: {}, TradeId: {}", 
                request.getCompensationType(), request.getOrderId(), request.getTradeId());

        return portfolioWebClient.post()
                .uri("/api/v1/compensation/trade")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PortfolioResponse.class)
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        log.info("Trade compensation successful → Type: {}, OrderId: {}, Message: {}", 
                                request.getCompensationType(), request.getOrderId(), response.getMessage());
                    } else {
                        log.error("Trade compensation failed → Type: {}, OrderId: {}, Error: {}, ErrorCode: {}", 
                                request.getCompensationType(), request.getOrderId(), 
                                response.getMessage(), response.getErrorCode());
                    }
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> validateAccountBalance(Long accountId, BigDecimal requiredAmount) {
        log.info("Account balance validation requested → AccountId: {}, RequiredAmount: {}", 
                accountId, requiredAmount);

        return portfolioWebClient.get()
                .uri("/api/v1/accounts/" + accountId)
                .retrieve()
                .bodyToMono(AccountBalanceResponse.class)
                .map(account -> {
                    // Calculate total required with commission (0.2%)
                    BigDecimal commission = requiredAmount.multiply(new BigDecimal("0.002"));
                    BigDecimal totalRequired = requiredAmount.add(commission);
                    
                    if (account.getAvailableBalance().compareTo(totalRequired) >= 0) {
                        log.info("Balance validation PASSED → AccountId: {}, Available: {}, Required: {}", 
                                accountId, account.getAvailableBalance(), totalRequired);
                        return PortfolioResponse.success("Sufficient balance", account);
                    } else {
                        String message = String.format("Yetersiz bakiye. Gerekli: %.2f TL, Mevcut: %.2f TL", 
                                totalRequired.doubleValue(), account.getAvailableBalance().doubleValue());
                        log.warn("Balance validation FAILED → AccountId: {}, Message: {}", accountId, message);
                        return PortfolioResponse.failure(message, "INSUFFICIENT_BALANCE");
                    }
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<PortfolioResponse> validateStockPosition(Long accountId, String symbol, Integer requiredQuantity) {
        log.info("Stock position validation requested → AccountId: {}, Symbol: {}, RequiredQuantity: {}", 
                accountId, symbol, requiredQuantity);

        return portfolioWebClient.get()
                .uri("/api/v1/stocks/" + accountId)
                .retrieve()
                .bodyToFlux(StockPositionResponse.class)
                .collectList()
                .map(stocks -> {
                    // Find total quantity for the symbol
                    BigDecimal availableQuantity = stocks.stream()
                            .filter(stock -> stock.getSymbol().equals(symbol))
                            .map(StockPositionResponse::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal requiredBigDecimal = new BigDecimal(requiredQuantity);
                    
                    if (availableQuantity.compareTo(requiredBigDecimal) >= 0) {
                        log.info("Stock validation PASSED → AccountId: {}, Symbol: {}, Available: {}, Required: {}", 
                                accountId, symbol, availableQuantity, requiredQuantity);
                        return PortfolioResponse.success("Sufficient stock quantity", stocks);
                    } else {
                        String message = String.format("Yetersiz %s hissesi. Mevcut: %s adet, Gerekli: %d adet", 
                                symbol, availableQuantity.toString(), requiredQuantity);
                        log.warn("Stock validation FAILED → AccountId: {}, Symbol: {}, Message: {}", 
                                accountId, symbol, message);
                        return PortfolioResponse.failure(message, "INSUFFICIENT_STOCK");
                    }
                })
                .onErrorMap(this::mapWebClientException)
                .toFuture();
    }

    @Override
    public CompletableFuture<Boolean> isPortfolioServiceHealthy() {
        log.debug("Checking Portfolio Service health");

        return portfolioWebClient.get()
                .uri(healthEndpoint)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false)
                .timeout(Duration.ofSeconds(3))
                .toFuture();
    }

    // FALLBACK METHODS

    public CompletableFuture<PortfolioResponse> reserveBuyStockFallback(BuyStockRequest request, Exception ex) {
        log.error("Buy stock reservation fallback triggered → OrderId: {}, Error: {}", 
                request.getOrderId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for buy reservation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> reserveSellStockFallback(SellStockRequest request, Exception ex) {
        log.error("Sell stock reservation fallback triggered → OrderId: {}, Error: {}", 
                request.getOrderId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for sell reservation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> confirmBuyTradeFallback(TradeConfirmationRequest request, Exception ex) {
        log.error("Buy trade confirmation fallback triggered → TradeId: {}, Error: {}", 
                request.getTradeId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for buy confirmation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> confirmSellTradeFallback(TradeConfirmationRequest request, Exception ex) {
        log.error("Sell trade confirmation fallback triggered → TradeId: {}, Error: {}", 
                request.getTradeId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for sell confirmation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> confirmPartialBuyTradeFallback(PartialTradeConfirmationRequest request, Exception ex) {
        log.error("Partial buy trade confirmation fallback triggered → TradeId: {}, Error: {}", 
                request.getTradeId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for partial buy confirmation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> confirmPartialSellTradeFallback(PartialTradeConfirmationRequest request, Exception ex) {
        log.error("Partial sell trade confirmation fallback triggered → TradeId: {}, Error: {}", 
                request.getTradeId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for partial sell confirmation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> cancelBuyOrderFallback(OrderCancellationRequest request, Exception ex) {
        log.error("Buy order cancellation fallback triggered → OrderId: {}, Error: {}", 
                request.getOrderId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for buy cancellation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<PortfolioResponse> cancelSellOrderFallback(OrderCancellationRequest request, Exception ex) {
        log.error("Sell order cancellation fallback triggered → OrderId: {}, Error: {}", 
                request.getOrderId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for sell cancellation", "SERVICE_UNAVAILABLE")
        );
    }

    public CompletableFuture<Boolean> isPortfolioServiceHealthyFallback(Exception ex) {
        log.warn("Portfolio Service health check fallback triggered → Error: {}", ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<PortfolioResponse> compensateTradeFallback(CompensationRequest request, Exception ex) {
        log.error("Trade compensation fallback triggered → Type: {}, OrderId: {}, TradeId: {}, Error: {}", 
                request.getCompensationType(), request.getOrderId(), request.getTradeId(), ex.getMessage());
        return CompletableFuture.completedFuture(
                PortfolioResponse.failure("Portfolio service unavailable for compensation", "SERVICE_UNAVAILABLE")
        );
    }

    // ERROR MAPPING

    private Throwable mapWebClientException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientEx) {
            String responseBody = webClientEx.getResponseBodyAsString();
            HttpStatus statusCode = (HttpStatus) webClientEx.getStatusCode();

            log.error("Portfolio Service HTTP Error → Status: {}, Body: {}", statusCode, responseBody);

            // Map specific HTTP errors to business exceptions
            return switch (statusCode) {
                case BAD_REQUEST -> {
                    if (responseBody.contains("INSUFFICIENT_BALANCE")) {
                        yield new InsufficientBalanceException(
                                "Account has insufficient balance for this operation", null
                        );
                    }
                    if (responseBody.contains("INSUFFICIENT_STOCK")) {
                        yield new InsufficientStockException(
                                "Account has insufficient stock for this operation", null, null
                        );
                    }
                    yield new PortfolioServiceException("Invalid request to Portfolio Service", "INVALID_REQUEST");
                }
                case NOT_FOUND -> new PortfolioServiceException("Account or resource not found", "NOT_FOUND");
                case CONFLICT -> new PortfolioServiceException("Portfolio state conflict", "CONFLICT");
                case SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR -> 
                        new PortfolioServiceException("Portfolio Service is temporarily unavailable", "SERVICE_UNAVAILABLE");
                case REQUEST_TIMEOUT, GATEWAY_TIMEOUT -> 
                        new PortfolioServiceException("Portfolio Service request timeout", "TIMEOUT");
                default -> new PortfolioServiceException(
                        String.format("Portfolio Service error: %s", webClientEx.getMessage()), 
                        "HTTP_ERROR"
                );
            };
        }

        // Non-HTTP errors (connection, timeout, etc.)
        return new PortfolioServiceException(
                String.format("Portfolio Service communication error: %s", throwable.getMessage()),
                "COMMUNICATION_ERROR",
                throwable
        );
    }
}