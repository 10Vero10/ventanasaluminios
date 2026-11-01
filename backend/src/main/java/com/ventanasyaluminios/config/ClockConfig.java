package com.ventanasyaluminios.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj único en UTC. Los services lo reciben inyectado para poder probar fechas con {@code Clock.fixed}. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
