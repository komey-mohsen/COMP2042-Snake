# Snake Game — Issues Report

Date: 3 November 2026

## Scope and evidence

Reviewed the root project (`src/main`), which is the version launched with the root `pom.xml`. Evidence includes the successful Maven compilation and launch, startup output, inspection of the running gameplay screen, and source review. Findings labelled **code review** follow directly from implementation; their reproduction steps are proposed checks, not claims that every scenario was played through. The separate `CW2026/` copy was not audited. No game code was changed.

## Summary

The game starts after working around its launcher configuration, but defects affect controls, power-up availability, obstacle placement, winning, restarting, and fullscreen rendering. Fix gameplay correctness before adding features or polishing presentation.

## High-priority defects

### 1. Down allows an illegal reversal

**Evidence:** Code review — `src/main/java/com/comp2042/GameController.java:125`, especially line 130.

The Down-key condition checks whether the current direction is **Down**, instead of checking **Up**. A player moving upward can reverse directly downward into the snake. Other opposite-direction checks reject reversals, so controls behave inconsistently.

**Check:** Move upward, wait for a movement tick, then press Down. It should retain the upward direction.

**Fix:** Reject Down when the current direction is Up. Check all four direction pairs.

### 2. Rainbow coins never spawn

**Evidence:** Code review — `src/main/java/com/comp2042/GameController.java:175`.

Both the gold branch and the following rainbow branch test `ran == 1`. Gold consumes every matching result, making the rainbow branch unreachable. Players cannot obtain the implemented speed bonus through normal play.

**Check:** Inspect the random-result mapping; no value reaches the rainbow constructor.

**Fix:** Assign rainbow a distinct random result and verify every intended coin type is reachable.

### 3. Obstacles can be outside the board or duplicated

**Evidence:** Code review — `src/main/java/com/comp2042/Obstacle.java:20`.

`rnd.nextInt(x) + 1` produces horizontal coordinates from 1 through the board width. Valid coordinates are 0 through width minus one. On the 20-column board, column 0 never gets obstacles and column 20 lies off the canvas. Placement also does not reject existing obstacle positions, allowing duplicates.

This creates fewer distinct playable obstacles than the list count suggests and makes the win-target adjustment inaccurate.

**Check:** Generate obstacles repeatedly and check bounds and uniqueness.

**Fix:** Use `rnd.nextInt(x)`, reject occupied positions, and count unique obstacles actually placed on the board.

### 4. The obstacle-adjusted win target is off by one

**Evidence:** Code review — `src/main/java/com/comp2042/Game.java:31`, `Game.java:48`, and `GameController.java:168`.

The initial snake occupies two cells, so a board with `N` cells and `K` valid, unique obstacles needs `N - 2 - K` collected coins to fill it. `decMaxScore(K)` subtracts only `K - 1`. Even with otherwise valid obstacles, the target requires one extra coin.

`Coin` searches indefinitely for a free location (`src/main/java/com/comp2042/Coin.java:17`). If the board fills before the target is reached, generating the next coin can block the JavaFX thread and freeze the interface.

**Fix:** Derive the target from available cells, check for completion before spawning, and explicitly handle a board with no free cells.

### 5. Restart retains the previous game's reduced win target

**Evidence:** Code review — `src/main/java/com/comp2042/Game.java:60` and `GameController.java:136`.

After a silver coin reduces `maxScore`, Tab clears obstacles and resets scores but never restores `maxScore`. The new empty board can therefore declare victory early. Collecting silver coins across subsequent restarts reduces the target further.

**Check:** Collect silver, restart, and inspect the target against a fresh game's target.

**Fix:** Reinitialize the target as part of resetting the complete game state, preferably from current board occupancy.

### 6. Winning does not finish the game cleanly

**Evidence:** Code review — `src/main/java/com/comp2042/GameController.java:63`.

On the winning collection, the handler redraws and returns before updating the score label, showing the win label, or releasing the per-tick input lock. It also leaves the animation running. On the next tick, the snake continues moving; a completed board can turn into a self-collision and show game-over behavior after winning.

**Check:** Reach a winning state and verify that the final score and victory appear immediately and movement stops.

**Fix:** Enter an explicit won state, update labels, stop the timeline, and allow only restart/menu actions.

### 7. Returning to the menu leaves the game timeline active

**Evidence:** Code review — `src/main/java/com/comp2042/GameController.java:118` and `MainMenuController.java:20`.

Escape shows the menu and closes the game window without stopping the animation. A live game can continue moving, spawning coins, and updating its hidden view. Starting again creates another controller while the old timeline has no explicit cleanup. Direction is also stored in static fields, so new sessions inherit shared direction state.

**Check:** Escape during movement and inspect whether the old timeline stops; start another game and check its initial direction.

**Fix:** Stop the timeline when leaving or closing the game, and keep direction as per-game state initialized for every session.

## Medium-priority defects

### 8. Wrapped movement skips obstacle collision at the destination

**Evidence:** Code review — `src/main/java/com/comp2042/Snake.java:40`.

Each movement branch checks obstacles before wrapping the head to the opposite edge. An obstacle at the wrapped destination is never checked on that move. A blue power-up can therefore let the snake enter an obstacle cell through an edge without dying.

**Check:** With border wrapping active, cross an edge whose opposite destination contains an obstacle.

**Fix:** Compute the final destination, including wrapping, before testing collisions.

### 9. Short snakes can overlap themselves

**Evidence:** Code review — `src/main/java/com/comp2042/Snake.java:88`.

Self-collision is checked only when `body.size() > 4`. A shorter snake is allowed to enter occupied body cells, which is especially visible when combined with the Up-to-Down reversal defect.

**Fix:** Test collisions against occupied cells for every length, accounting for a tail cell that moves away on a non-growing tick.

### 10. Fullscreen draws the snake at the wrong size

**Evidence:** Code review — `src/main/java/com/comp2042/GameView.java:132` and line 173.

Snake image dimensions multiply by `fullScale` twice. Grid positions, coins, and obstacles scale once. At a scale of 2, snake sprites become four times their original size while cell spacing only doubles, so sprites overlap neighboring cells and misrepresent collision boundaries.

**Check:** Enter fullscreen on a display where the scale is greater than one and compare snake size with obstacle cells.

**Fix:** Apply one scale factor consistently to sprite dimensions and positions.

### 11. Restart preserves stale body-turn graphics

**Evidence:** Code review — `src/main/java/com/comp2042/GameView.java:33`, line 157, and `GameController.java:136`.

Turning records corner information in `turnArray`. Restart creates a new snake but reuses the same view and never clears this array. Old corner markers can make straight segments in the new game render as bends.

**Check:** Make several turns, restart before the tail clears them, then cross those cells with a longer snake.

**Fix:** Clear turn state on restart or derive segment orientation from current segment positions.

### 12. Standard launch instructions do not work

**Evidence:** Observed during launch — `.mvn/wrapper/maven-wrapper.properties:1` and `README.md`.

`mvnw.cmd javafx:run` first failed because `JAVA_HOME` was unset. After selecting the installed JDK, it failed again because the wrapper URL requests `apache-maven-3-bin.zip` under version `3`, which returned a file-not-found error. The README contains only the project title.

The game compiled and launched using a separate temporary Maven 3.9.9 installation and the installed JDK 25. This workaround does not repair the repository launcher.

**Fix:** Pin a valid Maven distribution in the wrapper, document Java prerequisites and the launch command, and verify a fresh checkout.

## Usability and maintenance findings

### 13. Controls and power-up rules are undiscoverable

**Evidence:** Gameplay screen observation and code review — `MainMenu.fxml`, `GameController.java:85`, and `MainMenuController.java:11`.

The gameplay screen shows the board and score without control instructions or a pause indicator. The menu definition contains only a title and Start button. Players must discover arrow-key movement, P to stop, arrows to resume, Tab to restart, Escape to return, and the fullscreen shortcuts themselves. Coin effects and their duration are also unexplained.

Difficulty, board dimensions, and power-ups are hardcoded; the implemented easy/hard options cannot be selected in the menu.

**Improvement:** Add visible controls, a pause message, a coin legend, and settings for the options the game already implements.

### 14. Rendering does unnecessary work as the snake grows

**Evidence:** Code review — `src/main/java/com/comp2042/GameView.java:135` and `Snake.java:118`.

Each segment draw repeatedly calls `getBody()`, which copies the entire body list, and creates snapshot images during rendering. Repeating whole-list copies per segment creates approximately quadratic list-copy work per frame. This is a performance risk for longer snakes; no frame-rate benchmark was performed.

**Improvement:** Obtain one body snapshot per frame and cache rotated sprite images.

### 15. Verification and project clarity are weak

**Evidence:** Repository inspection and runtime output.

No automated tests were found. The FXML declares JavaFX 26 while the POM uses 25.0.1; launch emits a version warning, although the application loads. A second project copy in `CW2026/` makes it less obvious which source tree should be run or maintained. Unused sound imports/path and bonus-score code also obscure which features are active.

**Improvement:** Add focused checks for direction validation, obstacle bounds/uniqueness, restart state, and win transitions; align FXML/runtime versions; document the authoritative project location and remove or complete dead code.

## Recommended repair order

1. Correct direction validation and collision handling.
2. Fix obstacle generation, available-cell counting, and coin spawning on a full board.
3. Make win, restart, and menu transitions reset or dispose all relevant state.
4. Restore rainbow spawning and correct fullscreen/turn rendering.
5. Repair the wrapper and document setup and controls.
6. Add targeted regression checks and then address rendering efficiency and menu options.
