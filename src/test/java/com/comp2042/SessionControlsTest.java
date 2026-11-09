package com.comp2042;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionControlsTest {
    @Test
    void closingStopsTheClockAndLateInputsCannotRestartIt() {
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger stops = new AtomicInteger();
        SessionControls session = new SessionControls(starts::incrementAndGet, stops::incrementAndGet);
        session.requestDirection(Direction.UP);
        session.close();
        session.close();
        session.reset();
        assertFalse(session.requestDirection(Direction.LEFT));
        assertEquals(1, starts.get());
        assertEquals(1, stops.get());
        assertEquals(SessionControls.State.CLOSED, session.getState());
    }

    @Test
    void sessionsDoNotShareDirectionsOrInputLocks() {
        SessionControls first = new SessionControls(() -> {}, () -> {});
        SessionControls second = new SessionControls(() -> {}, () -> {});
        first.requestDirection(Direction.UP);
        assertEquals(Direction.LEFT, second.getDirection());
        assertEquals(SessionControls.State.READY, second.getState());
        assertTrue(second.requestDirection(Direction.DOWN));
        assertEquals(Direction.UP, first.getDirection());
        assertEquals(Direction.DOWN, second.getDirection());
        first.close();
        assertEquals(SessionControls.State.RUNNING, second.getState());
    }

    @Test
    void acceptsOnlyOneTurnBetweenTicksAndNeverReverses() {
        SessionControls session = new SessionControls(() -> {}, () -> {});
        assertTrue(session.requestDirection(Direction.UP));
        assertFalse(session.requestDirection(Direction.RIGHT));
        assertEquals(Direction.UP, session.getDirection());
        session.tickCompleted();
        assertFalse(session.requestDirection(Direction.DOWN));
        assertTrue(session.requestDirection(Direction.RIGHT));
        assertEquals(Direction.UP, session.getPreviousDirection());
        assertEquals(Direction.RIGHT, session.getDirection());
    }

    @Test
    void pauseAndArrowResumeKeepTheOneTurnPerTickLock() {
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger stops = new AtomicInteger();
        SessionControls session = new SessionControls(starts::incrementAndGet, stops::incrementAndGet);
        session.requestDirection(Direction.UP);
        session.pause();
        session.pause();
        assertEquals(SessionControls.State.PAUSED, session.getState());
        assertFalse(session.requestDirection(Direction.RIGHT));
        assertEquals(SessionControls.State.RUNNING, session.getState());
        assertEquals(Direction.UP, session.getDirection());
        assertEquals(2, starts.get());
        assertEquals(1, stops.get());
    }

    @Test
    void completedRoundCannotResumeButRestartRestoresDefaults() {
        SessionControls session = new SessionControls(() -> {}, () -> {});
        session.requestDirection(Direction.UP);
        session.finish();
        assertFalse(session.requestDirection(Direction.LEFT));
        assertEquals(SessionControls.State.FINISHED, session.getState());
        session.reset();
        assertEquals(SessionControls.State.READY, session.getState());
        assertEquals(Direction.LEFT, session.getDirection());
        assertEquals(Direction.LEFT, session.getPreviousDirection());
        assertTrue(session.requestDirection(Direction.DOWN));
    }
}
