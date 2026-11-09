package com.comp2042;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class HighScoreStoreTest {
    @TempDir Path directory;
    private final ScoreMode normal = new ScoreMode('N', true, 20, 20);
    private Path file() { return directory.resolve("scores/high-scores.properties"); }
    private HighScoreStore store() { return new HighScoreStore(file(), Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC)); }

    @Test
    void missingFileIsAnEmptyLeaderboardWithoutCreatingUserData() {
        var snapshot = store().snapshot(normal);
        assertTrue(snapshot.entries().isEmpty());
        assertTrue(snapshot.storageAvailable());
        assertFalse(Files.exists(file()));
    }

    @Test
    void scoresPersistAcrossNewStoreInstancesAndIncludeComboAndTime() {
        HighScoreStore first = store();
        assertTrue(first.record(normal, 100, 4));
        var entry = store().snapshot(normal).entries().get(0);
        assertEquals(100, entry.points());
        assertEquals(4, entry.bestCombo());
        assertEquals(Instant.parse("2026-11-10T00:00:00Z"), entry.recordedAt());
        assertFalse(first.snapshot(normal).pending());
    }

    @Test
    void onlyTheTenHighestScoresAreRetainedAndRepeatedReadsDoNotDuplicate() {
        HighScoreStore scores = store();
        for (int points = 1; points <= 15; points++) assertTrue(scores.record(normal, points, 1));
        assertEquals(List.of(15,14,13,12,11,10,9,8,7,6),
                store().snapshot(normal).entries().stream().map(HighScoreEntry::points).toList());
        assertTrue(scores.flush());
        assertEquals(10, store().snapshot(normal).entries().size());
    }

    @Test
    void difficultyPowerUpsAndBoardSizeHaveSeparateLeaderboards() {
        HighScoreStore scores = store();
        for (ScoreMode mode : List.of(normal, new ScoreMode('E', true, 20, 20),
                new ScoreMode('H', true, 20, 20), new ScoreMode('N', false, 20, 20), new ScoreMode('N', true, 10, 10))) {
            assertTrue(scores.record(mode, 50, 2));
        }
        assertEquals(1, store().snapshot(normal).entries().size());
        assertEquals(1, store().snapshot(new ScoreMode('N', false, 20, 20)).entries().size());
    }

    @Test
    void topTenLimitAppliesToEachModeRatherThanTheWholeFile() {
        HighScoreStore scores = store();
        ScoreMode hard = new ScoreMode('H', true, 20, 20);
        for (int points = 1; points <= 12; points++) {
            assertTrue(scores.record(normal, points, 1));
            assertTrue(scores.record(hard, points + 100, 2));
        }
        assertEquals(10, store().snapshot(normal).entries().size());
        assertEquals(10, store().snapshot(hard).entries().size());
    }

    @Test
    void zeroScoresAreSkippedAndInvalidScoresAreRejected() {
        HighScoreStore scores = store();
        assertTrue(scores.record(normal, 0, 1));
        assertFalse(Files.exists(file()));
        assertThrows(IllegalArgumentException.class, () -> scores.record(normal, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> scores.record(normal, 1, 6));
        assertThrows(IllegalArgumentException.class, () -> new ScoreMode('?', true, 20, 20));
    }

    @Test
    void anotherStoreInstanceCannotOverwritePreviouslySavedRounds() {
        HighScoreStore first = store(); HighScoreStore second = store();
        second.snapshot(normal);
        assertTrue(first.record(normal, 10, 1));
        assertTrue(second.record(normal, 20, 2));
        assertEquals(List.of(20, 10), store().snapshot(normal).entries().stream().map(HighScoreEntry::points).toList());
    }

    @Test
    void corruptDataIsPreservedWhileNewScoresRemainPendingUntilRecovery() throws Exception {
        Files.createDirectories(file().getParent());
        String corrupt = "version=1\ncount=not-a-number\n";
        Files.writeString(file(), corrupt);
        HighScoreStore scores = store();
        assertFalse(scores.snapshot(normal).storageAvailable());
        assertFalse(scores.record(normal, 30, 3));
        assertEquals(corrupt, Files.readString(file()));
        assertEquals(30, scores.snapshot(normal).entries().get(0).points());
        assertTrue(scores.snapshot(normal).pending());
        Files.writeString(file(), "version=1\ncount=0\n");
        assertTrue(scores.flush());
        assertTrue(scores.flush());
        assertEquals(1, store().snapshot(normal).entries().size());
    }

    @Test
    void fileLockFailureCanBeRetriedWithoutLosingOrDuplicatingTheScore() throws Exception {
        Files.createDirectories(file().getParent());
        Path lockPath = file().resolveSibling(file().getFileName() + ".lock");
        HighScoreStore scores = store();
        try (var channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                var lock = channel.lock()) {
            assertFalse(scores.record(normal, 75, 5));
            assertEquals(75, scores.snapshot(normal).entries().get(0).points());
            assertTrue(scores.snapshot(normal).pending());
        }
        assertTrue(scores.flush());
        assertEquals(1, store().snapshot(normal).entries().size());
    }

    @Test
    void unwritableLocationDoesNotCrashOrDiscardTheSessionScore() throws Exception {
        Path parentFile = directory.resolve("not-a-folder"); Files.writeString(parentFile, "keep me");
        HighScoreStore scores = new HighScoreStore(parentFile.resolve("scores.properties"));
        assertFalse(scores.record(normal, 20, 2));
        assertEquals(20, scores.snapshot(normal).entries().get(0).points());
        assertTrue(scores.snapshot(normal).pending());
        assertEquals("keep me", Files.readString(parentFile));
    }

    @Test
    void unknownFileVersionsAreNotOverwritten() throws Exception {
        Files.createDirectories(file().getParent());
        String futureData = "version=2\ncount=0\n";
        Files.writeString(file(), futureData);
        HighScoreStore scores = store();
        assertFalse(scores.record(normal, 100, 2));
        assertEquals(futureData, Files.readString(file()));
        assertTrue(scores.snapshot(normal).pending());
    }

    @Test
    void malformedPropertyEscapesDoNotCrashLoading() throws Exception {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), "version=1\ncount=0\nbroken=" + (char) 92 + "uXXXX");
        var snapshot = store().snapshot(normal);
        assertFalse(snapshot.storageAvailable());
        assertTrue(snapshot.entries().isEmpty());
    }
}
