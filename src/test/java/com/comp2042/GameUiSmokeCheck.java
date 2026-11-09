package com.comp2042;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
        System.out.println("PASS: UI construction, help layout, pause/resume, viewport scaling, menu cleanup, session isolation and window-close cleanup");
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
