package com.rupeek.maidbooking.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rupeek.maidbooking.constants.BookingConstants;


@Configuration
public class AppConfiguration {
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(BookingConstants.DEFAULT_TIME_ZONE));
    }
}
