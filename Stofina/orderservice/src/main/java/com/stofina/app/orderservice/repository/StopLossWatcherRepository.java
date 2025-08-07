package com.stofina.app.orderservice.repository;

import com.stofina.app.orderservice.entity.StopLossWatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StopLossWatcherRepository extends JpaRepository<StopLossWatcher, Long> {
    
    // Aktif watcher'ları getir
    List<StopLossWatcher> findByActiveTrue();
    
    // Tetiklenmemiş aktif watcher'ları getir
    List<StopLossWatcher> findByActiveTrueAndTriggeredFalse();
    
    // Belirli sembol için aktif watcher'ları getir
    List<StopLossWatcher> findBySymbolAndActiveTrueAndTriggeredFalse(String symbol);
    
    // Order ID ile watcher bul
    Optional<StopLossWatcher> findByOrderIdAndActiveTrue(Long orderId);
    
    // Belirli tarihten önce oluşturulan watcher'ları bul (cleanup için)
    List<StopLossWatcher> findByCreatedAtBeforeAndActiveTrue(LocalDateTime cutoffTime);
    
    // Belirli sembol için watcher sayısı
    @Query("SELECT COUNT(w) FROM StopLossWatcher w WHERE w.symbol = :symbol AND w.active = true AND w.triggered = false")
    Integer countActiveWatchersBySymbol(@Param("symbol") String symbol);
    
    // Soft delete - watcher'ı pasif yap
    @Query("UPDATE StopLossWatcher w SET w.active = false WHERE w.orderId = :orderId")
    void softDeleteByOrderId(@Param("orderId") Long orderId);
}