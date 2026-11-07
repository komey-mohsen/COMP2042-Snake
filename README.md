# COMP2042-Snake

A JavaFX desktop version of the classic Snake game, developed for the COMP2042 coursework project. Steer the snake, collect coins, and avoid collisions while exploring different difficulty levels and optional power-ups.

---

## 🚀 Features

- **Main menu:** A dark gradient interface with Play, Settings, and Quit buttons.
- **Difficulty settings:** Choose Easy, Normal, or Hard. Easy allows wrapping at the board edges, while Normal and Hard have walls; Hard also increases movement speed.
- **Coins and power-ups:** Collect coins to earn points, with optional special coins that introduce effects such as border wrapping and obstacles.
- **Restart and pause:** Pause a round or restart with a button or keyboard shortcut. Restart clears scores, obstacles, and active power-up effects.
- **Simple Windows launcher:** Double-click `Run Game.cmd` to find Java automatically, build the project, and launch the game.

---

## 🛠️ Prerequisites

Before you begin, ensure you have installed:

- **Java Development Kit (JDK) 23 or newer.** The project uses JavaFX 25.0.1, so a Java 21 installation alone is insufficient. The launcher has been tested with JDK 25.
- **Git**, if you want to clone the repository. Alternatively, download and extract the repository as a ZIP file.
- **Windows with Windows PowerShell** for the double-click launcher.

**Build tool:** Maven 3.9.9 is downloaded automatically by the included Maven Wrapper; a separate Maven installation is unnecessary. Internet access is required on the first launch to download Maven and project dependencies.

---

## 📦 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/komey-mohsen/COMP2042-Snake.git
   ```

2. Open the project folder:

   ```bash
   cd COMP2042-Snake
   ```

3. Start the game on Windows by double-clicking **Run Game.cmd** in the project folder. You can also run it from PowerShell:

   ```powershell
   & ".\Run Game.cmd"
   ```

4. Select **Settings** to choose your difficulty and enable or disable power-ups, then select **Back** and **Play**. Press an arrow key to start moving.

The first launch may take longer while dependencies download. Keep the launcher window open while playing. If startup fails, it stays open so you can read the error message.

Use the launcher in the root project folder, which builds `src/main`. The separate `CW2026/` directory is not used by this launcher.

---

## 🎮 Controls

| Control | Action |
| --- | --- |
| Arrow keys | Start moving and steer; resume after pausing |
| P | Pause the game |
| Restart button, R, or Tab | Reset the round; press an arrow key to start again |
| Escape | Return to the main menu |

---

## 🔧 Troubleshooting

- **Java cannot be found:** Install JDK 23 or newer. The launcher checks `JAVA_HOME`, Java on `PATH`, and common installation folders. For a JDK installed elsewhere, set `JAVA_HOME` to its installation folder and try again.
- **Downloads fail:** Check your internet connection and access to Maven Central, then rerun the launcher.
- **Changes do not appear:** Close the game and run `Run Game.cmd` again to rebuild the latest source files.
