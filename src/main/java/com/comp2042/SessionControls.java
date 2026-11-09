package com.comp2042;

import java.util.Objects;

/** Per-round input and clock lifecycle, independent of JavaFX and other game windows. */
public final class SessionControls implements AutoCloseable {
    public enum State { READY, RUNNING, PAUSED, FINISHED, CLOSED }

    private final Runnable startClock;
    private final Runnable stopClock;
    private State state = State.READY;
    private Direction direction = Direction.LEFT;
    private Direction previousDirection = Direction.LEFT;
    private boolean turnPending;

    public SessionControls(Runnable startClock, Runnable stopClock) {
        this.startClock = Objects.requireNonNull(startClock);
        this.stopClock = Objects.requireNonNull(stopClock);
    }

    /** Starts/resumes a live round and accepts at most one safe turn per movement tick. */
    public boolean requestDirection(Direction requested) {
        if (requested == null || state == State.CLOSED || state == State.FINISHED) return false;
        if (state != State.RUNNING) {
            state = State.RUNNING;
            startClock.run();
        }
        if (requested == direction || turnPending || !direction.canTurnTo(requested)) return false;
        previousDirection = direction;
        direction = requested;
        turnPending = true;
        return true;
    }

    /** Releases the input lock after a movement tick. */
    public void tickCompleted() { turnPending = false; }

    /** Pauses movement until the player presses an arrow key. */
    public void pause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
            stopClock.run();
        }
    }

    /** Stops a won/lost round and prevents arrow keys from restarting its clock. */
    public void finish() {
        if (state != State.CLOSED && state != State.FINISHED) {
            state = State.FINISHED;
            stopClock.run();
        }
    }

    /** Resets a live session to its initial direction and stopped, ready state. */
    public void reset() {
        if (state == State.CLOSED) return;
        stopClock.run();
        direction = previousDirection = Direction.LEFT;
        turnPending = false;
        state = State.READY;
    }

    /** Disposes a hidden/closed game window; subsequent inputs cannot resume it. */
    @Override public void close() {
        if (state != State.CLOSED) {
            state = State.CLOSED;
            turnPending = false;
            stopClock.run();
        }
    }

    public State getState() { return state; }
    public Direction getDirection() { return direction; }
    public Direction getPreviousDirection() { return previousDirection; }
}
