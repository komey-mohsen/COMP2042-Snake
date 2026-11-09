package com.comp2042;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Optional desktop check of the actual shaded JAR, run after Maven package. */
public final class PackagedJarSmokeCheck {
    private PackagedJarSmokeCheck() { }

    public static void main(String[] args) throws Exception {
        Path jar = Path.of("target", "snake-javafx.jar").toAbsolutePath();
        if (!Files.isRegularFile(jar)) throw new IllegalStateException("Run Maven package before the packaged-JAR check");
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Path output = Files.createTempFile("snake-packaged-launch-", ".log");
        Process game = null;
        try {
            game = new ProcessBuilder(java.toString(), "-jar", jar.toString())
                    .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (game.waitFor(3, TimeUnit.SECONDS)) {
                throw new AssertionError("Packaged game exited during startup: " + Files.readString(output));
            }
            System.out.println("PASS: java -jar starts the packaged game without missing JavaFX runtime errors");
        } finally {
            if (game != null && game.isAlive()) {
                game.destroy();
                if (!game.waitFor(5, TimeUnit.SECONDS)) {
                    game.destroyForcibly();
                    game.waitFor(5, TimeUnit.SECONDS);
                }
            }
            Files.deleteIfExists(output);
        }
    }
}
