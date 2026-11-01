package com.ventanasyaluminios.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contador en memoria de solicitudes por clave (la IP) en ventanas fijas: como máximo {@code maxRequests} por
 * {@code window}. Vale para una sola instancia del backend, que es como se despliega (ADR-004).
 */
public class RateLimiter {

    /** Cada tantas llamadas se borran las ventanas vencidas, para que el mapa no crezca sin límite. */
    private static final int CLEANUP_EVERY = 1_000;

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();

    public RateLimiter(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Registra una solicitud de {@code key}.
     *
     * @return cuánto falta para poder volver a intentar; {@link Duration#ZERO} si la solicitud está permitida
     */
    public Duration acquire(String key) {
        Instant now = clock.instant();
        if (calls.incrementAndGet() % CLEANUP_EVERY == 0) {
            windows.values().removeIf(current -> current.isExpired(now, window));
        }
        Window current = windows.compute(key, (k, previous) ->
                previous == null || previous.isExpired(now, window) ? new Window(now, 1) : previous.increment());
        if (current.count() <= maxRequests) {
            return Duration.ZERO;
        }
        return Duration.between(now, current.start().plus(window));
    }

    private record Window(Instant start, int count) {

        Window increment() {
            return new Window(start, count + 1);
        }

        boolean isExpired(Instant now, Duration window) {
            return !now.isBefore(start.plus(window));
        }
    }
}
