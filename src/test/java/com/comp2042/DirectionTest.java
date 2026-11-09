package com.comp2042;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {
    @Test
    void rejectsEachOppositeDirection() {
        assertFalse(Direction.UP.canTurnTo(Direction.DOWN));
        assertFalse(Direction.DOWN.canTurnTo(Direction.UP));
        assertFalse(Direction.LEFT.canTurnTo(Direction.RIGHT));
        assertFalse(Direction.RIGHT.canTurnTo(Direction.LEFT));
    }

    @Test
    void acceptsStraightAndPerpendicularMoves() {
        for (Direction direction : Direction.values()) assertTrue(direction.canTurnTo(direction));
        assertTrue(Direction.UP.canTurnTo(Direction.LEFT));
        assertTrue(Direction.UP.canTurnTo(Direction.RIGHT));
        assertTrue(Direction.DOWN.canTurnTo(Direction.LEFT));
        assertTrue(Direction.DOWN.canTurnTo(Direction.RIGHT));
        assertTrue(Direction.LEFT.canTurnTo(Direction.UP));
        assertTrue(Direction.LEFT.canTurnTo(Direction.DOWN));
        assertTrue(Direction.RIGHT.canTurnTo(Direction.UP));
        assertTrue(Direction.RIGHT.canTurnTo(Direction.DOWN));
        assertFalse(Direction.UP.canTurnTo(null));
    }
}
