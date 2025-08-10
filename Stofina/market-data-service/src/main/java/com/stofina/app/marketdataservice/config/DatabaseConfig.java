package com.stofina.app.marketdataservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@Configuration
public class DatabaseConfig {

    //TODO: Database bilgileri geldikten sonra bu sınıfı güncelle

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Istanbul")));
    }
}
