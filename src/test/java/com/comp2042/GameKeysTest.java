package com.comp2042;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class GameKeysTest {
    @ParameterizedTest
    @CsvSource({"UP,UP", "KP_UP,UP", "DOWN,DOWN", "KP_DOWN,DOWN", "LEFT,LEFT", "KP_LEFT,LEFT", "RIGHT,RIGHT", "KP_RIGHT,RIGHT"})
    void arrowVariantsUseTheSameGameplayDirection(KeyCode key, Direction direction) {
        assertEquals(direction, GameKeys.directionFor(key));
    }

    @Test
    void everyArrowFlaggedByJavaFxIsHandledAndOtherKeysAreIgnored() {
        for (KeyCode key : KeyCode.values()) {
            if (key.isArrowKey()) assertNotNull(GameKeys.directionFor(key), key.name());
            else assertNull(GameKeys.directionFor(key), key.name());
        }
        assertNull(GameKeys.directionFor(null));
    }
}
