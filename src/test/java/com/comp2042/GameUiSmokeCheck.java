package com.comp2042;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Optional desktop integration check; excluded from normal headless JUnit execution. */
public class GameUiSmokeCheck {
    static class CountingSnake extends Snake {
        int snapshots;
        CountingSnake(Obstacle obstacles) { super(20, 20, false, obstacles); }
        @Override public ArrayList<Point> getBody() { snapshots++; return super.getBody(); }
    }

    static void checkRendering(GameView view) throws Exception {
        Obstacle obstacles = new Obstacle(20, 20);
        CountingSnake snake = new CountingSnake(obstacles);
        for (int i = 0; i < 9; i++) {
            Point next = snake.getHead(); next.translate(-1, 0); snake.move('L', next);
        }
        Point turn = snake.getHead(); turn.translate(0, 1); snake.move('D', turn);
        view.drawGrid(null, snake, obstacles);
        check(snake.snapshots == 1, "Rendering must obtain exactly one body snapshot per frame");
        Map<?, ?> cache = (Map<?, ?>) field(view, "spriteImages");
        check(cache.size() == 4, "Sprite cache should contain only the four original images");
        Map<?, ?> originalImages = new HashMap<>(cache);
        for (int i = 0; i < 20; i++) view.drawGrid(null, snake, obstacles);
        check(snake.snapshots == 21, "Body copying must remain once per frame as the snake grows");
        check(originalImages.equals(cache), "Redrawing should reuse existing sprite images");
        check(view.gc.getTransform().getMxx() == 1 && view.gc.getTransform().getMyy() == 1
                && view.gc.getTransform().getTx() == 0 && view.gc.getTransform().getTy() == 0,
                "Sprite rotations must not leak into the next drawing operation");
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object);
    }
    static GameController controller(Stage gameStage) throws Exception {
        Object handler = gameStage.getOnHidden();
        for (Field field : handler.getClass().getDeclaredFields()) {
            if (field.getType() == GameController.class) { field.setAccessible(true); return (GameController) field.get(handler); }
        }
        throw new AssertionError("No session disposal handler");
    }
    static void key(Scene scene, KeyCode code) {
        scene.getOnKeyPressed().handle(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }
    static void play(ViewManager views) {
        Button play = (Button) views.getPrimaryStage().getScene().getRoot().lookupAll(".button").stream()
            .filter(n -> n instanceof Button b && b.getText().equals("Play")).findFirst().orElseThrow();
        play.fire();
    }
    static void runChecks() throws Exception {
        new Main().start(new Stage());
        ViewManager views = Main.getViewManager();
        play(views);
        Stage gameStage = views.getGameStage();
        Scene scene = gameStage.getScene();
        scene.getRoot().applyCss(); scene.getRoot().layout();
        GameController controller = controller(gameStage);
        GameView view = (GameView) field(controller, "gameView");
        Timeline clock = (Timeline) field(controller, "animation");
        check(view.statusLabel.isVisible(), "Start prompt is missing");
        BorderPane root = (BorderPane) scene.getRoot();
        StackPane area = (StackPane) root.getCenter();
        check(view.gridCanvas.getWidth() <= area.getWidth(), "Board is clipped horizontally");
        check(view.gridCanvas.getHeight() <= area.getHeight(), "Board overlaps help footer");
        checkRendering(view);
        key(scene, KeyCode.UP);
        check(clock.getStatus() == Animation.Status.RUNNING, "Arrow did not start clock");
        key(scene, KeyCode.P);
        check(clock.getStatus() == Animation.Status.STOPPED, "Pause did not stop clock");
        check(view.statusLabel.isVisible() && view.statusLabel.getText().contains("PAUSED"), "Pause prompt missing");
        key(scene, KeyCode.UP);
        check(!view.statusLabel.isVisible(), "Pause overlay did not clear on resume");
        controller.fullscreenChanged(true);
        controller.resizeBoard(800, 700);
        check(view.gridCanvas.getWidth() == 700 && view.gridCanvas.getHeight() == 700, "Fullscreen board doesn't fit viewport");
        controller.fullscreenChanged(false);
        check(view.gridCanvas.getWidth() == 400 && view.gridCanvas.getHeight() == 400, "Windowed board didn't restore");
        key(scene, KeyCode.ESCAPE);
        check(clock.getStatus() == Animation.Status.STOPPED, "Menu return left timeline active");
        check(!gameStage.isShowing() && views.getPrimaryStage().isShowing(), "Menu return didn't swap windows");
        key(scene, KeyCode.DOWN);
        check(clock.getStatus() == Animation.Status.STOPPED, "Closed session resumed from late input");
        play(views);
        Stage secondStage = views.getGameStage();
        GameController second = controller(secondStage);
        SessionControls controls = (SessionControls) field(second, "controls");
        check(controls.getDirection() == Direction.LEFT && controls.getState() == SessionControls.State.READY, "New game inherited old session state");
        key(secondStage.getScene(), KeyCode.DOWN);
        Timeline secondClock = (Timeline) field(second, "animation");
        secondStage.close();
        check(secondClock.getStatus() == Animation.Status.STOPPED, "Closing window left timeline active");
        System.out.println("PASS: cached rendering, one body snapshot per frame, UI layout, pause/resume, viewport scaling and session cleanup");
    }
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> Platform.setImplicitExit(false));
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> { try { runChecks(); } catch (Throwable t) { failure.set(t); }
            finally { for (Window window : new ArrayList<>(Window.getWindows())) window.hide(); done.countDown(); Platform.exit(); } });
        if (!done.await(30, TimeUnit.SECONDS)) throw new AssertionError("Integration probe timed out");
        if (failure.get() != null) throw new AssertionError("Integration probe failed", failure.get());
    }
}
