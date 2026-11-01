package com.ventanasyaluminios.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-01T12:00:00Z"));
    private final RateLimiter limiter = new RateLimiter(10, Duration.ofHours(1), clock);

    @Test
    void permiteDiezEnviosPorHoraYBloqueaElOnceConElTiempoQueFalta() {
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.acquire("1.1.1.1")).isZero();
        }
        clock.advance(Duration.ofMinutes(15));

        assertThat(limiter.acquire("1.1.1.1")).isEqualTo(Duration.ofMinutes(45));
    }

    @Test
    void cadaIpTieneSuPropioContador() {
        for (int i = 0; i < 11; i++) {
            limiter.acquire("1.1.1.1");
        }

        assertThat(limiter.acquire("2.2.2.2")).isZero();
    }

    @Test
    void alTerminarLaVentanaSeVuelveAPermitir() {
        for (int i = 0; i < 11; i++) {
            limiter.acquire("1.1.1.1");
        }
        clock.advance(Duration.ofHours(1));

        assertThat(limiter.acquire("1.1.1.1")).isZero();
    }

    /** Reloj que la prueba adelanta a mano. */
    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
