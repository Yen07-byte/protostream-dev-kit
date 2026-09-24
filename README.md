# 🎮 ProtoStream Game Dev Kit
> [License: PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

Notice: This project is source-available under the **PolyForm Noncommercial License 1.0.0**. You are free to modify, share, and use this Dev Kit for any personal or non-commercial purpose. While you cannot sell the resulting `.protogame` files or use them for commercial exploitation, you are fully permitted to create files for both the free and paid versions of **Yen's ProtoStream**. Developers are also welcome to accept voluntary tips or donations for their creations on platforms like Patreon or Ko-fi, provided a fully functional, free version of the `.protogame` file remains publicly available.

---

Welcome to the ProtoStream Dev Kit! This template provides everything you need to write custom Java games for the ProtoStream Matrix platform.

---

## 🚀 Quick Start Guide

### 1. Project Setup
You will need to clone this repository in **Android Studio**. If you haven't used Android Studio before, follow these steps to get started:

1. If you are currently in an open project, click `File -> Close Project`.
2. On the welcome screen, click **Clone Repository**.
3. Paste the URL of this repository into the URL field and click **Clone**.
4. Wait for the initial Gradle sync to complete and load all dependencies.

---

### 2. Write Your Game

1. Navigate to `app/src/main/java/com/protostream/devkit/` in the Project Explorer on the left.
2. Open `SampleGame.java`. This is your main game class!
3. Fill out the `@GameInfo` annotation at the top with your game's `title`, `author`, and `description`.
4. Write your initialization code in `onInit()`, game logic in `onUpdate()`, and visual drawing code in `onRender()`.
5. *(Optional)* You can create helper Java classes in the same package. Just ensure your main entry class extends `ProtoGame` and includes the `@GameInfo` annotation.

> 💡 **Note on Class Renaming:** If you rename `SampleGame`, remember to also rename the Java file (`SampleGame.java -> YourNewName.java`) using Android Studio's **Refactor -> Rename** option (`Shift + F6`).

---

### 3. Build Your Game

When you are ready to compile your project into a `.protogame` package:

1. Open the **Gradle** tab on the far-right panel of Android Studio.
2. Click the **Execute Gradle Task** icon (the small box with a play arrow).
3. In the popup bar, type `buildProtoGame` and press **Enter**.

Android Studio will automatically compile your code, package your metadata, and output a `.protogame` file.

---

### 4. Import to ProtoStream

1. Expand the `app/output/` folder in the Android Studio Project Explorer.
2. Copy your generated `.protogame` file to your mobile device.
3. Open the **ProtoStream** app, navigate to **Games Plugin Manager -> Custom Games**, and tap **Import .protogame**!

---

## 🛠️ Complete API Reference

### 1. Game Lifecycle & Timing

Every custom game extends `ProtoGame`. The engine executes your code inside a dedicated game thread running at approximately **30 FPS (~33ms per frame)**.

```java
public class MyGame extends ProtoGame {

    @Override
    public void onInit() {
        // Called ONCE when the game is loaded.
        // Use this to initialize variables, object pools, and starfields.
    }

    @Override
    public void onUpdate(Input input) {
        // Called EVERY FRAME (~30 times/sec) before rendering.
        // Use this for game logic, physics updates, and reading player input.
    }

    @Override
    public void onRender(ProtoCanvas canvas) {
        // Called EVERY FRAME immediately after onUpdate().
        // Use this to clear the buffer and draw shapes, text, or pixels.
    }
}
```

---

### 2. Annotation Metadata (`@GameInfo`)

The `@GameInfo` annotation allows ProtoStream's Custom Game manager to index and display your game's details without launching the engine.

```java
@GameInfo(
    title = "Space Defender",
    author = "ProtoStream Devs",
    description = "Retro arcade shooter with starfields and particle effects."
)
public class SampleGame extends ProtoGame { ... }
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `title` | `String` | The display name of your game in the ProtoStream launcher. |
| `author` | `String` | Your name or developer handle. |
| `description` | `String` | A brief summary of gameplay or instructions. |

---

### 3. Display & Canvas (`ProtoCanvas`)

The `ProtoCanvas` provides lower-level drawing primitives mapped to the **128 x 32 pixel LED matrix**.

*   **Coordinates:** $(x, y)$ where $(0,0)$ is the **Top-Left** corner, and $(127, 31)$ is the **Bottom-Right** corner.
*   **Color Format:** 32-bit ARGB Hexadecimal Integers (`0xAARRGGBB`).

```java
// Set screen background to dark slate
canvas.clear(0xFF121212);

// Draw a single pixel at x=10, y=15 (Red)
canvas.setPixel(10, 15, 0xFFEF4444);

// Fill a rectangle at x=20, y=10 with width=12, height=6 (Blue)
canvas.fillRect(20, 10, 12, 6, 0xFF3B82F6);

// Render text at x=2, y=10 (ProtoStream Green)
canvas.drawText("SCORE: 100", 2, 10, 0xFF10B981);
```

#### Canvas Method Summary

| Method | Parameters | Description |
| :--- | :--- | :--- |
| `clear(color)` | `int color` | Fills the entire $128 \times 32$ canvas with a solid ARGB color. |
| `setPixel(x, y, color)` | `int x, int y, int color` | Sets an individual pixel at $(x, y)$. Values out of bounds are ignored. |
| `fillRect(x, y, w, h, color)`| `int x, int y, int width, int height, int color` | Fills a solid rectangle from $(x, y)$ extending $w$ pixels right and $h$ pixels down. |
| `drawText(text, x, y, color)` | `String text, int x, int y, int color` | Draws text using a bold 10pt monospace font. |

> ⚠️ **Important Note on Text Coordinates:** `drawText` uses standard Android text baselines. The `y` parameter represents the **bottom baseline** of the text. To render text visibly near the top of the screen, set $y \approx 8 \text{ to } 10$. Setting $y = 0$ will cause the text to draw off-screen above the matrix!

---

### 4. Input System (`Input`)

The `Input` interface handles controller state across physical gamepads, touch screen overlay buttons, and hardware keyboards.

```java
if (input.isPressed(Input.Button.A)) {
    // True while the A button, Enter key, Spacebar, or Gamepad A button is held down
}
```

#### Available Buttons

| Button Enum | Keyboard Mapping | Touch Overlay | Gamepad Hardware Mapping |
| :--- | :--- | :--- | :--- |
| `Input.Button.UP` | `W` / Up Arrow | D-Pad Up | D-Pad Up / Left Stick Up |
| `Input.Button.DOWN` | `S` / Down Arrow | D-Pad Down | D-Pad Down / Left Stick Down |
| `Input.Button.LEFT` | `A` / Left Arrow | D-Pad Left | D-Pad Left / Left Stick Left |
| `Input.Button.RIGHT` | `D` / Right Arrow | D-Pad Right | D-Pad Right / Left Stick Right |
| `Input.Button.A` | `Z` / `Space` / `Enter` | A Button | Gamepad Button A / Cross |
| `Input.Button.B` | `X` | B Button | Gamepad Button B / Circle |
| `Input.Button.START` | `P` | - | Gamepad Start Button |
| `Input.Button.SELECT` | `Tab` | - | Gamepad Select Button |

#### Button Debouncing (Edge Triggering)

`input.isPressed()` returns `true` continuously every frame for as long as a button is held down. To trigger an action **once per press** (such as firing a laser, pausing, or navigating menus), track the button's previous state:

```java
private boolean lastStartPressed = false;

@Override
public void onUpdate(Input input) {
    boolean currentStart = input.isPressed(Input.Button.START);
    
    // Check if the button was pressed THIS frame for the first time
    if (currentStart && !lastStartPressed) {
        togglePauseMenu();
    }
    
    lastStartPressed = currentStart;
}
```

---

## ⚡ Performance Guidelines & Best Practices

1. **Avoid In-Loop Garbage Collection (GC Thrashing):**
   * Do **not** use `new` inside `onUpdate()` or `onRender()` (e.g., `new Particle()` or string concatenations like `"Score: " + score`).
   * Pre-allocate entity arrays, objects, and strings during `onInit()`, then reuse them across frames. GC pauses on Android will cause visible stutter on the LED matrix stream.
2. **Keep Text Concise:**
   * Due to the $128 \times 32$ aspect ratio, space is limited. Use short labels (e.g., `"S:100"` instead of `"SCORE: 100"`) to conserve horizontal real estate.
3. **Optimized Screen Refreshing:**
   * Always call `canvas.clear(...)` at the start of `onRender()` to flush the frame buffer before drawing the next scene.
4. **Be Wary of Matrix Obstructions:**
   * Some external parts of the helmet may obstruct the visor (for example, in JTingF's models, the side fin rings block the $\approx 20 \times 20$ areas in the bottom left and right corners). Keep vital game assets (such as UI elements) in view.
