package de.ronnywalter.eve.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Configuration
@Slf4j
public class LocaleConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Set timezone to " + TimeZone.getDefault().toZoneId().toString());
        log.info("Time is now: " + LocalDateTime.now().toString());
    }
}