package com.comp2042;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/** Local top-ten storage with atomic replacement, cross-process locking and retryable pending scores. */
public final class HighScoreStore {
    public static final int LIMIT = 10;
    private static final Comparator<HighScoreEntry> ORDER = Comparator.comparingInt(HighScoreEntry::points)
            .reversed().thenComparing(HighScoreEntry::recordedAt).thenComparing(HighScoreEntry::id);
    private final Path file;
    private final Clock clock;
    private final Map<UUID, HighScoreEntry> pending = new LinkedHashMap<>();
    private List<HighScoreEntry> cached = List.of();
    private boolean available = true;

    public record Snapshot(List<HighScoreEntry> entries, boolean pending, boolean storageAvailable) {
        public Snapshot { entries = List.copyOf(entries); }
    }

    public HighScoreStore(Path file) { this(file, Clock.systemUTC()); }

    HighScoreStore(Path file, Clock clock) {
        this.file = file.toAbsolutePath();
        this.clock = clock;
    }

    public static HighScoreStore local() {
        return new HighScoreStore(Path.of(System.getProperty("user.home"), ".comp2042-snake", "high-scores.properties"));
    }

    /** Zero-point rounds are omitted; failed writes remain visible in memory for a later retry. */
    public synchronized boolean record(ScoreMode mode, int points, int bestCombo) {
        if (points == 0) return true;
        HighScoreEntry entry = new HighScoreEntry(UUID.randomUUID(), mode, points, bestCombo, clock.instant());
        pending.put(entry.id(), entry);
        return flush();
    }

    public synchronized Snapshot snapshot(ScoreMode mode) {
        try { cached = read(); available = true; }
        catch (IOException | SecurityException error) { available = false; }
        return new Snapshot(merged().stream().filter(entry -> entry.mode().equals(mode)).sorted(ORDER).limit(LIMIT).toList(),
                !pending.isEmpty(), available);
    }

    /** Re-reads under a file lock so other game instances' saved rounds are preserved. */
    public synchronized boolean flush() {
        if (pending.isEmpty()) return true;
        try {
            Files.createDirectories(file.getParent());
            Path lockFile = file.resolveSibling(file.getFileName() + ".lock");
            try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    var lock = channel.tryLock()) {
                if (lock == null) throw new IOException("Score store is busy");
                cached = read();
                List<HighScoreEntry> entries = trimmed(merged());
                write(entries);
                cached = entries;
                pending.clear();
                available = true;
                return true;
            }
        } catch (IOException | SecurityException | OverlappingFileLockException error) {
            available = false;
            return false;
        }
    }

    private List<HighScoreEntry> merged() {
        Map<UUID, HighScoreEntry> entries = new LinkedHashMap<>();
        for (HighScoreEntry entry : cached) entries.put(entry.id(), entry);
        entries.putAll(pending);
        return new ArrayList<>(entries.values());
    }

    private List<HighScoreEntry> trimmed(List<HighScoreEntry> entries) {
        Map<ScoreMode, Integer> counts = new HashMap<>();
        return entries.stream().sorted(ORDER).filter(entry -> counts.merge(entry.mode(), 1, Integer::sum) <= LIMIT).toList();
    }

    private List<HighScoreEntry> read() throws IOException {
        if (!Files.exists(file)) return List.of();
        if (!Files.isRegularFile(file) || Files.size(file) > 1_048_576) throw new IOException("Invalid score file");
        Properties data = new Properties();
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { data.load(reader); }
        catch (IllegalArgumentException invalid) { throw new IOException("Invalid score data", invalid); }
        try {
            if (!"1".equals(data.getProperty("version"))) throw new IllegalArgumentException("Unknown score format");
            int count = Integer.parseInt(data.getProperty("count"));
            if (count < 0 || count > 10000) throw new IllegalArgumentException("Invalid entry count");
            List<HighScoreEntry> entries = new ArrayList<>();
            for (int index = 0; index < count; index++) {
                String key = "entry." + index + ".";
                String powerUps = data.getProperty(key + "powerUps");
                if (!"true".equals(powerUps) && !"false".equals(powerUps)) throw new IllegalArgumentException("Invalid mode");
                String difficulty = data.getProperty(key + "difficulty");
                if (difficulty == null || difficulty.length() != 1) throw new IllegalArgumentException("Invalid difficulty");
                ScoreMode mode = new ScoreMode(difficulty.charAt(0), Boolean.parseBoolean(powerUps),
                        Integer.parseInt(data.getProperty(key + "width")), Integer.parseInt(data.getProperty(key + "height")));
                entries.add(new HighScoreEntry(UUID.fromString(data.getProperty(key + "id")), mode,
                        Integer.parseInt(data.getProperty(key + "points")), Integer.parseInt(data.getProperty(key + "combo")),
                        Instant.parse(data.getProperty(key + "at"))));
            }
            return trimmed(entries);
        } catch (RuntimeException invalid) { throw new IOException("Invalid score data", invalid); }
    }

    private void write(List<HighScoreEntry> entries) throws IOException {
        Properties data = new Properties();
        data.setProperty("version", "1");
        data.setProperty("count", Integer.toString(entries.size()));
        for (int index = 0; index < entries.size(); index++) {
            HighScoreEntry entry = entries.get(index);
            String key = "entry." + index + ".";
            data.setProperty(key + "id", entry.id().toString());
            data.setProperty(key + "difficulty", Character.toString(entry.mode().difficulty()));
            data.setProperty(key + "powerUps", Boolean.toString(entry.mode().powerUps()));
            data.setProperty(key + "width", Integer.toString(entry.mode().width()));
            data.setProperty(key + "height", Integer.toString(entry.mode().height()));
            data.setProperty(key + "points", Integer.toString(entry.points()));
            data.setProperty(key + "combo", Integer.toString(entry.bestCombo()));
            data.setProperty(key + "at", entry.recordedAt().toString());
        }
        Path temporary = Files.createTempFile(file.getParent(), "scores-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(writer, "COMP2042 Snake local high scores");
            }
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
}
