package com.comp2042;

/** Plain Java entry point for the shaded JAR; the existing JavaFX application remains Main. */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        Main.main(args);
    }
}
