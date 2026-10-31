# 🎮 ProtoStream Game Dev Kit

> [License: PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

Notice: This project is source-available under the **PolyForm Noncommercial License 1.0.0**. You are free to modify, share, and use this Dev Kit for any personal or non-commercial purpose. While you cannot sell the resulting `.protogame` files or use them for commercial exploitation, you are fully permitted to create files for both the free and paid versions of **Yen's ProtoStream**. Developers are also welcome to accept voluntary tips or donations for their creations on platforms like Patreon or Ko-fi, provided a fully functional, free version of the `.protogame` file remains publicly available.

Welcome to the ProtoStream Dev Kit! This template provides everything you need to write, preview, and package custom Java or Kotlin games for the ProtoStream Matrix platform.

## 🚀 Quick Start Guide

### 1. Project Setup

You will need to clone this repository in **Android Studio**. If you haven't used Android Studio before, follow these steps to get started:

1. If you are currently in an open project, click `File -> Close Project`.

2. On the welcome screen, click **Clone Repository**.

3. Paste the URL of this repository into the URL field and click **Clone**.

4. Wait for the initial Gradle sync to complete and load all dependencies.

### 2. Write Your Game & Add Sprites

1. Navigate to `app/src/main/java/com/protostream/devkit/` in the Project Explorer on the left.

2. Open `SampleGame.java`. This is your main game class!

3. Fill out the `@GameInfo` annotation at the top with your game's `title`, `author`, and `description`.

4. *(Optional)* Place any custom PNG, GIF, BMP, or WebP sprite images inside `app/src/main/assets/` (for example, `player_sheet.png` or `heart.png`). They will automatically be bundled into your `.protogame` file!

5. Write your initialization code in `onInit()`, game logic in `onUpdate()`, and visual drawing code in `onRender()`.

6. *(Optional)* You can create helper classes in the same package. Just ensure your main entry class extends `ProtoGame` and includes the `@GameInfo` annotation.

> 💡 **Note on Class Renaming:** If you rename `SampleGame`, remember to also rename the Java file (`SampleGame.java -> YourNewName.java`) using Android Studio's **Refactor -> Rename** option (`Shift + F6`), and update `new SampleGame()` in `PreviewActivity.java` if you use the live emulator preview.

### 3. Live Test in Android Studio (Emulator or Phone)

Before packaging your `.protogame` file, you can test and step-debug your game right inside Android Studio:

1. Select an Android Emulator or connected phone in the top toolbar of Android Studio.

2. Click the green **Run ▶** button (`Shift + F10`).

3. The built-in **`PreviewActivity`** will launch a scaled `128 x 32` LED matrix simulator complete with on-screen **D-Pad**, **A**, **B**, **SELECT**, **START**, and a simulated **BOOP** button!

### 4. Build Your `.protogame` Package

When you are ready to compile your project into a `.protogame` package:

1. Open the **Gradle** tab on the far-right panel of Android Studio.

2. Click the **Execute Gradle Task** icon (the small box with a play arrow).

3. In the popup bar, type `buildProtoGame` and press **Enter**.

Android Studio will automatically compile your code, package your `metadata.json`, bundle every image inside `app/src/main/assets/`, and output a `.protogame` file in `app/output/`.

### 5. Import to ProtoStream

1. Expand the `app/output/` folder in the Android Studio Project Explorer.

2. Copy your generated `.protogame` file to your mobile device.

3. Open the **ProtoStream** app, navigate to **Games Plugin Manager -> Custom Games**, and tap **Import .protogame**!

## 🛠️ Complete API Reference

### 1. Game Lifecycle, Timing & Frame Counter

Every custom game extends `ProtoGame`. The engine executes your code inside a dedicated game thread running at approximately **30 FPS (~33ms per frame)**.

```java
public class MyGame extends ProtoGame {

    @Override
    public void onInit() {
        // Called ONCE when the game is loaded.
        // Load saved high scores and pre-allocate entity pools here.
    }

    @Override
    public void onUpdate(Input input) {
        // Called EVERY FRAME (~30 times/sec) before rendering.
        // Read buttons/sensors and update game physics here.
        long tick = getFrameCount(); // Auto-incremented frame counter (0, 1, 2, ...)
    }

    @Override
    public void onRender(ProtoCanvas canvas) {
        // Called EVERY FRAME immediately after onUpdate().
        // Clear the buffer and draw sprites, shapes, text, or pixels.
    }
}
```

### 2. Annotation Metadata (`@GameInfo`)

The `@GameInfo` annotation allows ProtoStream's Custom Game manager to index and display your game's details without launching the engine.

```java
@GameInfo(
    title = "Space Defender DX",
    author = "ProtoStream Devs",
    description = "Retro arcade shooter with PNG sprite sheets, weapon switching, and saved high scores."
)
public class SampleGame extends ProtoGame { ... }
```

| Field | Type | Description |
| ----- | ----- | ----- |
| `title` | `String` | The display name of your game in the ProtoStream launcher. |
| `author` | `String` | Your name or developer handle. |
| `description` | `String` | A brief summary of gameplay or instructions. |

### 3. Display, Primitives & Fonts (`ProtoCanvas`)

The `ProtoCanvas` provides drawing primitives and sprite renderers mapped to the **128 x 32 pixel LED matrix**.

* **Coordinates:** $(x, y)$ where $(0,0)$ is the **Top-Left** corner, and $(127, 31)$ is the **Bottom-Right** corner.

* **Color Format:** 32-bit ARGB Hexadecimal Integers (`0xAARRGGBB`). Pixels with alpha `0x00` are transparent; semi-transparent pixels (`0x01..0xFE`) automatically alpha-blend over the canvas.

```java
// Clear screen to deep space slate
canvas.clear(0xFF0A0A12);

// Draw a 1px horizontal HUD divider line
canvas.drawLine(0, 6, 127, 6, 0xFF1E293B);

// Draw ultra-compact 3x5 pixel HUD text (top-left at x=2, y=1)
canvas.drawSmallText("SC:120 HI:450", 2, 1, 0xFF10B981);

// Draw an unfilled rectangle box and a circle shockwave
canvas.drawRect(10, 8, 20, 12, 0xFF3B82F6);
canvas.drawCircle(64, 16, 10, 0xFF38BDF8);
canvas.fillCircle(64, 16, 3, 0xFFF59E0B);

// Draw standard bold 10pt monospace text (y is bottom baseline)
canvas.drawText("PAUSED", 46, 18, 0xFFF59E0B);
```

#### Canvas Method Summary

| Method | Parameters | Description |
| ----- | ----- | ----- |
| `clear(color)` | `int color` | Fills the entire $128 	imes 32$ canvas with a solid ARGB color. |
| `setPixel(x, y, color)` | `int x, int y, int color` | Sets an individual pixel at $(x, y)$ with alpha-blending support. |
| `fillRect(x, y, w, h, color)` | `int x, int y, int width, int height, int color` | Fills a solid rectangle from $(x, y)$ extending $w$ pixels right and $h$ pixels down. |
| `drawRect(x, y, w, h, color)` | `int x, int y, int width, int height, int color` | Draws a 1-pixel unfilled rectangle outline. |
| `drawLine(x1, y1, x2, y2, color)` | `int x1, int y1, int x2, int y2, int color` | Draws a crisp 1-pixel line between $(x_1, y_1)$ and $(x_2, y_2)$. |
| `drawCircle(cx, cy, r, color)` | `int cx, int cy, int radius, int color` | Draws a 1-pixel circle outline centered at $(cx, cy)$. |
| `fillCircle(cx, cy, r, color)` | `int cx, int cy, int radius, int color` | Draws a solid filled circle centered at $(cx, cy)$. |
| `drawText(text, x, y, color)` | `String text, int x, int y, int color` | Draws text using a bold 10pt monospace font (`y` = bottom baseline; use $y pprox 8	ext{..}10$ for top text). |
| `drawSmallText(text, x, y, color)` | `String text, int x, int y, int color` | Draws compact `3x5` pixel bitmap font text (`(x, y)` = **top-left** corner; 4px horizontal stride per character). |
| `drawImage(name, x, y)` | `String assetName, int x, int y` | Draws an image from `app/src/main/assets/` at top-left $(x, y)$ with full alpha transparency. |
| `drawImageFlipped(...)` | `String assetName, int x, int y, boolean flipX, boolean flipY` | Draws an image from `app/src/main/assets/` with optional horizontal/vertical mirroring. |
| `drawSpriteFrame(...)` | `String assetName, int frameW, int frameH, int frameIndex, int x, int y, boolean flipX, boolean flipY` | Draws a single animation frame (`0, 1, 2, ...` left-to-right, top-to-bottom) from a sprite sheet in `assets/`. |
| `drawPixelMap(...)` | `int[] pixels, int w, int h, int x, int y, boolean flipX, boolean flipY` | Draws an in-code 1D ARGB pixel array of size `w * h` at $(x, y)$ (`0x00000000` = transparent). |

### 4. Importing Sprites, Sprite Sheets & Pixel Maps

You do **not** need to draw every graphic manually with `setPixel` or `fillRect`! There are three easy ways to render sprites:

#### Method A: Drag-and-Drop Image Files (`app/src/main/assets/`)

1. Create your sprite or icon in any pixel-art editor (such as Aseprite, Piskel, LibreSprite, or Photoshop) at visor scale (for example, `8x6` or `16x16` pixels) and export it as a `.png` with a transparent background.

2. Drop the `.png` file into `app/src/main/assets/` in Android Studio (for example, `app/src/main/assets/heart.png`).

3. Call `canvas.drawImage("heart.png", x, y)` or `canvas.drawImageFlipped("heart.png", x, y, flipX, flipY)` inside `onRender()`!

```java
// Draw app/src/main/assets/heart.png at (2, 1)
canvas.drawImage("heart.png", 2, 1);

// Draw mirrored horizontally
canvas.drawImageFlipped("player.png", 20, 12, true, false);
```

#### Method B: Animated Sprite Sheets (`drawSpriteFrame`)

To animate a character or object, place all frames side by side in a single horizontal strip or grid PNG (for example, `player_sheet.png` measuring `24x6` pixels containing three `8x6` frames):

```java
// Advance animation frame every 3 game ticks (10 FPS animation at 30 FPS game loop)
int frame = (int) ((getFrameCount() / 3) % 3);

// Draw frame (0, 1, or 2) of size 8x6 from player_sheet.png at (playerX, playerY)
canvas.drawSpriteFrame("player_sheet.png", 8, 6, frame, (int) playerX, (int) playerY, false, false);
```

#### Method C: In-Code ARGB Pixel Arrays (`drawPixelMap`)

For tiny icons or projectiles that you want to keep directly inside your Java/Kotlin source file without an external PNG:

```java
private static final int C = 0xFF38BDF8; // Cyan
private static final int W = 0xFFE0F2FE; // White
private static final int _ = 0x00000000; // Transparent

private static final int[] SHIELD_SPRITE = new int[] {
    _, C, C, C, _,
    C, W, C, W, C,
    C, C, W, C, C,
    _, C, C, C, _,
    _, _, C, _, _
};

// Inside onRender(ProtoCanvas canvas):
canvas.drawPixelMap(SHIELD_SPRITE, 5, 5, 22, 1, false, false);
```

### 5. Input System & Visor Sensor Hooks (`Input`)

The `Input` interface handles controller state across physical gamepads, touch-screen overlay buttons (including on-screen **D-Pad**, **A**, **B**, **SELECT**, and **START**), hardware keyboards, and visor sensors.

#### Available Buttons

| Button Enum | Keyboard Mapping | Touch Overlay | Gamepad Hardware Mapping |
| ----- | ----- | ----- | ----- |
| `Input.Button.UP` | `W` / Up Arrow | D-Pad Up | D-Pad Up / Left Stick Up |
| `Input.Button.DOWN` | `S` / Down Arrow | D-Pad Down | D-Pad Down / Left Stick Down |
| `Input.Button.LEFT` | `A` / Left Arrow | D-Pad Left | D-Pad Left / Left Stick Left |
| `Input.Button.RIGHT` | `D` / Right Arrow | D-Pad Right | D-Pad Right / Left Stick Right |
| `Input.Button.A` | `Z` / `Space` / `Enter` | A Button | Button A / Cross / R1 / R2 / Right Trigger |
| `Input.Button.B` | `X` | B Button | Button B / Circle / X / Y / L1 / L2 / Left Trigger |
| `Input.Button.START` | `P` | Start Button | Gamepad Start Button |
| `Input.Button.SELECT` | `Tab` | Select Button | Gamepad Select Button |

> 💡 **Visor Boop & Head Gesture Support:** Any visor **Boop Detection** or **Head & Motion Gesture** configured in ProtoStream to emit a controller button or trigger input automatically maps to its corresponding `Input.Button` inside custom games!

#### Continuous Hold vs. Built-In Edge Triggering (`isPressed` vs. `isJustPressed`)

* **`input.isPressed(button)`**: Returns `true` every frame while the button is held down (ideal for continuous ship/character movement).
* **`input.isJustPressed(button)`**: Returns `true` **only on the initial frame** the button is pressed down (ideal for shooting, jumping, pausing with `START`, or switching modes with `SELECT`—no manual boolean debouncing required!).
* **`input.isJustReleased(button)`**: Returns `true` only on the frame the button is released.

```java
@Override
public void onUpdate(Input input) {
    // Single-press pause toggle using built-in edge detection
    if (input.isJustPressed(Input.Button.START)) {
        togglePauseMenu();
    }

    // Single-press weapon switch using SELECT
    if (input.isJustPressed(Input.Button.SELECT)) {
        cycleWeaponMode();
    }

    // Continuous movement while held
    if (input.isPressed(Input.Button.LEFT)) {
        playerX -= 1.5f;
    }
}
```

#### Direct Visor Hardware & Sensor Methods

Your custom game can also react directly to visor Boops, 3D head/helmet tilt, and voice/microphone volume:

| Method | Return Type | Description |
| ----- | ----- | ----- |
| `input.isJustBooped()` | `boolean` | `true` on the exact frame the visor Boop sensor is triggered. |
| `input.isBooped()` | `boolean` | `true` while a visor Boop event or Boop overlay is active. |
| `input.getTiltX()` | `float` | Normalized horizontal roll tilt in `-1.0f .. 1.0f` (`< 0` left, `> 0` right). |
| `input.getTiltY()` | `float` | Normalized vertical pitch tilt in `-1.0f .. 1.0f` (`< 0` up/back, `> 0` down/forward). |
| `input.getMicLevel()` | `float` | Normalized ambient/voice microphone amplitude in `0.0f .. 1.0f` (when mic permission is granted). |

### 6. Saving High Scores & Persistent Data (`ProtoGame`)

Every custom game gets its own sandboxed `SharedPreferences` storage space in ProtoStream so high scores and unlocked progress persist even after closing the app:

```java
private int highScore = 0;

@Override
public void onInit() {
    // Load the saved high score (defaults to 0 if never saved)
    highScore = loadInt("highScore", 0);
}

private void onPlayerScored(int newScore) {
    if (newScore > highScore) {
        highScore = newScore;
        // Persist the new high score immediately
        saveInt("highScore", highScore);
    }
}
```

* `saveInt(String key, int value)` / `loadInt(String key, int defaultValue)`
* `saveString(String key, String value)` / `loadString(String key, String defaultValue)`

## ⚡ Performance Guidelines & Best Practices

1. **Avoid In-Loop Garbage Collection (GC Thrashing):**

   * Do **not** allocate `new` objects inside `onUpdate()` or `onRender()` (e.g., `new Particle()`).

   * Pre-allocate entity arrays and objects during `onInit()`, then reuse them across frames. GC pauses on Android can cause visible stutter on the LED matrix stream.

2. **Use `drawSmallText` for Compact HUDs:**

   * Because the matrix is only $32$ pixels tall, use `canvas.drawSmallText(...)` (`3x5` pixel font) for scores, lives, and status bars so your gameplay area has maximum vertical room.

3. **Optimized Screen Refreshing:**

   * Always call `canvas.clear(...)` at the start of `onRender()` to flush the frame buffer before drawing the next scene.

4. **Be Wary of Matrix Obstructions:**

   * Some external parts of the helmet may obstruct the visor (for example, in JTingF's models, the side fin rings block the $pprox 20 	imes 20$ areas in the bottom left and right corners). Keep vital HUD elements near the top or center of the visor.
