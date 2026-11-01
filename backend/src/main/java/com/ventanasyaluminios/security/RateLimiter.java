package com.ventanasyaluminios.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contador en memoria de solicitudes por clave (la IP) en ventanas fijas: como máximo {@code maxRequests} por
 * {@code window}. Vale para una sola instancia del backend, que es como se despliega (ADR-004). La memoria está
 * acotada: si hay demasiadas claves distintas, las nuevas se rechazan hasta que venzan ventanas.
 */
public class RateLimiter {

    /** Cada tantas llamadas se borran las ventanas vencidas. */
    private static final int CLEANUP_EVERY = 1_000;

    /** Máximo de clientes distintos contados a la vez (unos pocos MB de memoria). */
    static final int DEFAULT_MAX_KEYS = 100_000;

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final int maxKeys;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();

    public RateLimiter(int maxRequests, Duration window, Clock clock) {
        this(maxRequests, window, clock, DEFAULT_MAX_KEYS);
    }

    RateLimiter(int maxRequests, Duration window, Clock clock, int maxKeys) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
        this.maxKeys = maxKeys;
    }

    /**
     * Registra una solicitud de {@code key}.
     *
     * @return cuánto falta para poder volver a intentar; {@link Duration#ZERO} si la solicitud está permitida
     */
    public Duration acquire(String key) {
        Instant now = clock.instant();
        if (calls.incrementAndGet() % CLEANUP_EVERY == 0) {
            removeExpired(now);
        }
        if (!windows.containsKey(key) && windows.size() >= maxKeys) {
            removeExpired(now);
            if (windows.size() >= maxKeys) {
                // Demasiados clientes distintos a la vez (posible rotación de IP): no se cuenta uno nuevo.
                return window;
            }
        }
        Window current = windows.compute(key, (k, previous) ->
                previous == null || previous.isExpired(now, window) ? new Window(now, 1) : previous.increment());
        if (current.count() <= maxRequests) {
            return Duration.ZERO;
        }
        return Duration.between(now, current.start().plus(window));
    }

    private void removeExpired(Instant now) {
        windows.values().removeIf(current -> current.isExpired(now, window));
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
