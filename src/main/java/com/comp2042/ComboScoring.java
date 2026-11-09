package com.comp2042;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Scores consecutive coins against a monotonic clock that advances only during active play. */
public final class ComboScoring {
    public static final int MAX_MULTIPLIER = 5;
    public static final Duration WINDOW = Duration.ofSeconds(5);
    private final LongSupplier clock;
    private long accumulated;
    private long resumedAt;
    private long lastCoinAt;
    private boolean running;
    private boolean collected;
    private int multiplier = 1;
    private int bestMultiplier = 1;

    public ComboScoring() { this(System::nanoTime); }

    ComboScoring(LongSupplier clock) { this.clock = Objects.requireNonNull(clock); }

    public void resume() {
        if (!running) { resumedAt = clock.getAsLong(); running = true; }
    }

    public void pause() {
        if (running) { accumulated = activeTime(); running = false; }
    }

    private long activeTime() { return accumulated + (running ? clock.getAsLong() - resumedAt : 0); }

    /** First coin is x1; a coin strictly before expiry raises the multiplier, capped at x5. */
    public int collect(int basePoints) {
        if (basePoints <= 0) throw new IllegalArgumentException("Coin points must be positive");
        long now = activeTime();
        int next = collected && now - lastCoinAt < WINDOW.toNanos()
                ? Math.min(MAX_MULTIPLIER, multiplier + 1) : 1;
        int points = Math.multiplyExact(basePoints, next);
        multiplier = next;
        bestMultiplier = Math.max(bestMultiplier, multiplier);
        lastCoinAt = now;
        collected = true;
        return points;
    }

    public Duration remaining() {
        return Duration.ofNanos(collected ? Math.max(0, WINDOW.toNanos() - (activeTime() - lastCoinAt)) : 0);
    }

    public int multiplier() { return remaining().isZero() ? 1 : multiplier; }
    public int bestMultiplier() { return bestMultiplier; }

    public void reset() {
        accumulated = resumedAt = lastCoinAt = 0;
        running = collected = false;
        multiplier = bestMultiplier = 1;
    }
}
