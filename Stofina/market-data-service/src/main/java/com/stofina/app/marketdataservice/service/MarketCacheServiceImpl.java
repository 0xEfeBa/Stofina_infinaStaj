package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.constant.Constants;
import com.stofina.app.marketdataservice.service.impl.IMarketCacheService;
import com.stofina.app.marketdataservice.util.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class MarketCacheServiceImpl implements IMarketCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> cachePrice(String symbol, BigDecimal price) { //Verilen hisse fiyatını Redis'e cache'ler (TTL ile sınırlı).
        String key = CacheKeyUtil.buildPriceKey(symbol);
        return redisTemplate.opsForValue()
                .set(key, price, Duration.ofSeconds(Constants.Cache.PRICE_TTL_SECONDS))// Cache TTL'si 5 saniye olarak ayarlandı
                .doOnSuccess(result -> log.info("{} sembolü için fiyat cachelendi: {}", symbol, price))
                .then();
    }

    @Override
    public Mono<BigDecimal> getCachedPrice(String symbol) { // Verilen hisse sembolü için cache'den fiyatı alır.
        String key = CacheKeyUtil.buildPriceKey(symbol);
        return redisTemplate.opsForValue()
                .get(key)
                .cast(BigDecimal.class)
                .doOnNext(price -> log.info("{} sembolü için cache'ten fiyat alındı: {}", symbol, price))
                .onErrorResume(e -> {
                    log.error("{} sembolü için cache fiyatı alınamadı: {}", symbol, e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> cacheDailyStats(String symbol, BigDecimal high, BigDecimal low) { // Günlük yüksek ve düşük fiyat istatistiklerini Redis'e cache'ler.
        String key = CacheKeyUtil.buildDailyStatsKey(symbol);
        BigDecimal[] stats = new BigDecimal[] { high, low };
        return redisTemplate.opsForValue()
                .set(key, stats)
                .doOnSuccess(result -> log.info("{} için günlük istatistikler cachelendi → Yüksek: {}, Düşük: {}", symbol, high, low))
                .then();
    }

    @Override
    public Mono<Void> addSubscriber(String symbol, String sessionId) { //WebSocket üzerinden hisseye abone olan kullanıcıyı Redis set'ine ekler.
        String key = CacheKeyUtil.buildSubscribersKey(symbol);
        return redisTemplate.opsForSet()
                .add(key, sessionId)
                .doOnNext(count -> log.info("{} oturum ID'si, {} sembolüne abone olarak eklendi.", sessionId, symbol))
                .then();
    }

    @Override
    public Mono<Void> removeSubscriber(String symbol, String sessionId) { //Kullanıcıyı ilgili hisse abonelik listesinden siler.
        String key = CacheKeyUtil.buildSubscribersKey(symbol);
        return redisTemplate.opsForSet()
                .remove(key, sessionId)
                .doOnNext(count -> log.info(" {} oturum ID'si, {} sembolünden abonelikten çıkarıldı.", sessionId, symbol))
                .then();
    }

    @Override
    public Flux<String> getSubscribers(String symbol) { // Belirli bir hisseye abone olan tüm kullanıcıları getirir
        String key = CacheKeyUtil.buildSubscribersKey(symbol);
        return redisTemplate.opsForSet()
                .members(key)
                .map(Object::toString)
                .doOnSubscribe(sub -> log.info("{} sembolü için abone listesi getiriliyor...", symbol));
    }

    @Override
    public Mono<Void> clearAllCache() { // Redis cache'ini tamamen temizler.
        return redisTemplate.execute(connection -> connection.serverCommands().flushAll())
                .doOnComplete(() -> log.warn("Redis cache tamamen temizlendi."))
                .then();
    }
}
