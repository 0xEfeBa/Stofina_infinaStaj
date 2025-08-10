package com.stofina.app.marketdataservice.constant;

import java.math.BigDecimal;

// CHECKPOINT 2.1: Constants Class - Centralized Configuration
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final class Stocks {
        public static final String AKBNK = "AKBNK";
        public static final String CCOLA = "CCOLA";
        public static final String DOAS = "DOAS";
        public static final String MGROS = "MGROS";
        public static final String FROTO = "FROTO";
        public static final String TCELL = "TCELL";
        public static final String THYAO = "THYAO";
        public static final String YEOTK = "YEOTK";
        public static final String BRSAN = "BRSAN";
        public static final String TUPRS = "TUPRS";

        public static final String AKBNK_NAME = "Akbank T.A.Ş.";
        public static final String CCOLA_NAME = "Coca-Cola İçecek A.Ş.";
        public static final String DOAS_NAME = "Doğuş Otomotiv Servis ve Ticaret A.Ş.";
        public static final String MGROS_NAME = "Migros Ticaret A.Ş.";
        public static final String FROTO_NAME = "Ford Otomotiv Sanayi A.Ş.";
        public static final String TCELL_NAME = "Turkcell İletişim Hizmetleri A.Ş.";
        public static final String THYAO_NAME = "Türk Hava Yolları A.O.";
        public static final String YEOTK_NAME = "Yeşil Otomotiv Endüstrisi ve Ticaret A.Ş.";
        public static final String BRSAN_NAME = "Bera Sanayi ve Ticaret A.Ş.";
        public static final String TUPRS_NAME = "Tüpraş-Türkiye Petrol Rafinerileri A.Ş.";

        public static final BigDecimal AKBNK_DEFAULT_PRICE = new BigDecimal("67.15");
        public static final BigDecimal CCOLA_DEFAULT_PRICE = new BigDecimal("49.92");
        public static final BigDecimal DOAS_DEFAULT_PRICE = new BigDecimal("183.10");
        public static final BigDecimal MGROS_DEFAULT_PRICE = new BigDecimal("531.00");
        public static final BigDecimal FROTO_DEFAULT_PRICE = new BigDecimal("92.95");
        public static final BigDecimal TCELL_DEFAULT_PRICE = new BigDecimal("92.55");
        public static final BigDecimal THYAO_DEFAULT_PRICE = new BigDecimal("290.25");
        public static final BigDecimal YEOTK_DEFAULT_PRICE = new BigDecimal("42.08");
        public static final BigDecimal BRSAN_DEFAULT_PRICE = new BigDecimal("354.00");
        public static final BigDecimal TUPRS_DEFAULT_PRICE = new BigDecimal("164.50");

        private Stocks() {
        }
    }

    public static final class MarketHours {
        public static final int MARKET_OPEN_HOUR = 9;
        public static final int MARKET_OPEN_MINUTE = 30;
        public static final int MARKET_CLOSE_HOUR = 18;
        public static final int MARKET_CLOSE_MINUTE = 0;
        public static final String TIMEZONE = "Europe/Istanbul";

        private MarketHours() {
        }
    }

    public static final class PriceSimulation {
        public static final long UPDATE_INTERVAL_MS = 10000L; // 10 saniye
        public static final double MAX_CHANGE_PERCENT = 0.02; // %2 maksimum değişim
        public static final double DAILY_LIMIT_PERCENT = 0.10; // Günlük ±%10 limit
        public static final int DECIMAL_PLACES = 2;
        public static final double MIN_VOLATILITY = 0.02;
        public static final double MAX_VOLATILITY = 0.05;

        private PriceSimulation() {
        }
    }

    public static final class Cache {
        public static final String PRICE_KEY_PREFIX = "market:price:";
        public static final String SESSION_KEY_PREFIX = "market:session:";
        public static final String SUBSCRIBERS_KEY_PREFIX = "market:subscribers:";
        public static final String DAILY_STATS_KEY_PREFIX = "market:stats:";
        public static final int PRICE_TTL_SECONDS = 5;
        public static final int SESSION_TTL_SECONDS = 3600;
        public static final int STATS_TTL_SECONDS = 86400;


        private Cache() {
        }
    }

    public static final class WebSocket {
        public static final String ENDPOINT = "/ws";
        public static final String TOPIC_PREFIX = "/topic";
        public static final String APP_PREFIX = "/app";
        public static final String PRICE_TOPIC = "/topic/market-data";
        public static final String MARKET_STATUS_TOPIC = "/topic/market-status";
        public static final String SYMBOL_TOPIC_PREFIX = "/topic/prices/";
        public static final String CONNECTION_TOPIC = "/topic/connections";
        public static final String ERROR_TOPIC = "/topic/errors";
        public static final String SUBSCRIBE_ENDPOINT = "/app/subscribe";
        public static final String UNSUBSCRIBE_ENDPOINT = "/app/unsubscribe";

        private WebSocket() {
        }
    }

    public static final class Scheduler {
        public static final int THREAD_POOL_SIZE = 5;
        public static final String THREAD_NAME_PREFIX = "market-scheduler-";
        public static final long STATUS_CHECK_INTERVAL_MS = 300000L; // 5 dakika

        private Scheduler() {
        }
    }

    public static final class AsyncTask {
        public static final int CORE_POOL_SIZE = 3;
        public static final int MAX_POOL_SIZE = 10;
        public static final int QUEUE_CAPACITY = 50;
        public static final String THREAD_NAME_PREFIX = "async-task-";

        private AsyncTask() {
        }
    }

    public static final class Validation {
        public static final int MAX_SYMBOL_LENGTH = 10;
        public static final int MIN_SYMBOL_LENGTH = 3;
        public static final int MAX_SYMBOLS_PER_REQUEST = 10;
        public static final String SYMBOL_PATTERN = "^[A-Z]+$";
        public static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
        public static final BigDecimal MAX_PRICE = new BigDecimal("99999.99");

        private Validation() {
        }
    }

    public static final class Api {
        public static final String BASE_PATH = "/api/v1/market";
        public static final String HEALTH_PATH = "/health";
        public static final String STATUS_PATH = "/status";
        public static final String SYMBOLS_PATH = "/symbols";
        public static final String PRICES_PATH = "/prices";

        private Api() {
        }
    }

    public static final class ErrorMessages {
        public static final String INVALID_SYMBOL = "Geçersiz sembol";
        public static final String STOCK_NOT_FOUND = "Hisse bulunamadı";
        public static final String MARKET_CLOSED = "Piyasa kapalı";
        public static final String INVALID_PRICE = "Geçersiz fiyat";
        public static final String REDIS_CONNECTION_ERROR = "Cache bağlantı hatası";
        public static final String INVALID_SYMBOL_FORMAT = "Sembol formatı geçersiz";
        public static final String SYMBOLS_LIST_EMPTY = "Sembol listesi boş olamaz";
        public static final String MAX_SYMBOLS_EXCEEDED = "Maksimum 10 sembol sorgulanabilir";
        public static final String INTERNAL_SERVER_ERROR = "Sunucu hatası";

        private ErrorMessages() {
        }
    }

    public static final class HttpStatus {
        public static final int OK = 200;
        public static final int BAD_REQUEST = 400;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;

        private HttpStatus() {
        }
    }
}