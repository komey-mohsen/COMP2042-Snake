package com.comp2042;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One completed or deliberately ended round; the ID prevents duplicate writes on retry. */
public record HighScoreEntry(UUID id, ScoreMode mode, int points, int bestCombo, Instant recordedAt) {
    public HighScoreEntry {
        Objects.requireNonNull(id);
        Objects.requireNonNull(mode);
        Objects.requireNonNull(recordedAt);
        if (points <= 0 || bestCombo < 1 || bestCombo > ComboScoring.MAX_MULTIPLIER) {
            throw new IllegalArgumentException("Invalid high score or combo");
        }
    }
}
