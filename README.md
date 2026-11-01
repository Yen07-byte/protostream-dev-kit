# 🎮 ProtoStream Game Dev Kit

> [License: PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

Notice: This project is source-available under the **PolyForm Noncommercial License 1.0.0**. You are free to modify, share, and use this Dev Kit for any personal or non-commercial purpose. While you cannot sell the resulting `.protogame` files or use them for commercial exploitation, you are fully permitted to create files for both the free and paid versions of **Yen's ProtoStream**. Developers are also welcome to accept voluntary tips or donations for their creations on platforms like Patreon or Ko-fi, provided a fully functional, free version of the `.protogame` file remains publicly available.

Welcome to the **ProtoStream Dev Kit**! This template provides everything you need to write, live-test, and package custom Java or Kotlin games for the **128 x 32 ProtoStream Matrix** platform—even if you have never used Android Studio before.

## 🧭 Beginner's Guide to Android Studio & Project Setup

### 1. Installing Android Studio (First-Time Users)

If you do not have Android Studio installed yet:

1. Download the official installer from [developer.android.com/studio](https://developer.android.com/studio).

2. Run the installer and keep all default checkboxes enabled (`Android SDK`, `Android SDK Platform`, and `Android Virtual Device`).

3. Launch Android Studio and step through the **Standard** initial setup wizard so it downloads the required Android SDK tools automatically.

### 2. Cloning & Opening the Dev Kit Project

1. Open **Android Studio**.
    * If you are on the **Welcome to Android Studio** screen, click **Get from VCS** (or **Clone Repository**).
    * If you already have another project open, click **File -> New -> Project from Version Control...** in the top menu bar.

2. In the **URL** field, paste the URL of this repository (`https://github.com/Yen07-byte/protostream-dev-kit`) and click **Clone**.

3. **Wait for Gradle Sync to Finish (Important!):**
    * Look at the **bottom-right corner** of the Android Studio window. You will see a progress bar that says `Gradle Build Running` or `Syncing...`.
    * The very first time you open the project, Android Studio downloads the build tools it needs (which can take 1–3 minutes depending on your internet connection). Wait until that bottom-right progress bar disappears before running or building your game.

### 3. Navigating Android Studio's Project Explorer (`Android` View vs. `Project` View)

On the **far-left side** of Android Studio is the **Project** panel (if it is minimized, click the folder icon labeled **Project** on the left sidebar).

At the very top of that panel is a dropdown menu that defaults to **Android**:

* **Android View (Default):** Groups your code and assets cleanly for daily coding:
    * `app -> manifests -> AndroidManifest.xml` *(Registers `PreviewActivity` for the live emulator)*
    * `app -> java -> com.protostream.devkit -> SampleGame` *(Your main game code!)*
    * `app -> java -> com.protostream.devkit -> PreviewActivity` *(Built-in live emulator preview)*
    * `app -> java -> com.protostream.devkit -> ProtoStreamSDK` *(SDK reference interfaces)*
    * `app -> assets` *(Folder where you drop `.png` sprite images)*
* **Project View (Shows Real Folders on Disk):** Click the **Android** dropdown at the top-left and switch it to **Project** whenever you want to see the actual folder tree on your computer—including the **`app/output/`** folder where your finished `.protogame` files are saved!

## 📱 Using the Built-In Mobile Device Emulator (Live Testing)

You do **not** need to export a `.protogame` file and copy it to your phone every time you want to test a change! The Dev Kit includes a built-in **`PreviewActivity`** that simulates the `128 x 32` LED matrix, on-screen gamepad (**D-Pad**, **A**, **B**, **SELECT**, **START**), and visor **BOOP** sensor right on your computer screen.

### Step 1: Create a Virtual Phone in Android Studio (One-Time Setup)

If you have never created an Android Emulator before, follow these steps once:

1. Look at the **right-hand sidebar** of Android Studio and click the **Device Manager** icon (it looks like a small mobile phone), or click **Tools -> Device Manager** in the top menu bar.

2. At the top of the **Device Manager** panel, click the **`+`** button and select **Create Virtual Device**.

3. Under the **Phone** category, select any modern device profile (such as **Pixel 7** or **Pixel 8**) and click **Next**.

4. On the **System Image** screen, look at the top recommended release (such as **API 34 ("UpsideDownCake")** or **API 35 ("VanillaIceCream")**):
    * If there is a blue **Download** icon next to the system image name, click it and wait for the download to complete, then click **Finish**.
    * Select that system image and click **Next**, then click **Finish**.

Your virtual phone now appears in the **Device Manager** list and in the device dropdown menu on Android Studio's top toolbar!

### Step 2: Launch Your Game in the Emulator

1. In the **top toolbar** of Android Studio, make sure the configuration dropdown says **`app`** and the target device dropdown next to it shows your virtual phone (e.g., **Pixel 7**).

2. Click the green **Run 'app' (▶)** play button in the top toolbar (or press `Shift + F10` on Windows/Linux, `Ctrl + R` on macOS).

3. The Android Emulator window will boot up and automatically launch **ProtoStream DevKit Live Preview**!

### Step 3: Controlling Your Game (On-Screen Touch, PC Keyboard & Xbox Controller)

Once `PreviewActivity` is running, you can control your game using any combination of mouse/touch, keyboard, or gamepad:

* **Mouse / Touch Overlay:** Click the on-screen **UP**, **DOWN**, **LEFT**, **RIGHT**, **A**, **B**, **SELECT**, **START**, and **BOOP** buttons directly inside the emulator window.
* **Computer Keyboard:** Click once on the dark matrix preview area inside the emulator so the emulator window has keyboard focus, then use your PC keyboard:
    * **Movement (D-Pad):** `W` / `A` / `S` / `D` or **Arrow Keys**
    * **A Button:** `Z`, `Spacebar`, or `Enter`
    * **B Button:** `X` or `Left/Right Shift`
    * **Start Button:** `P` or `Esc`
    * **Select Button:** `Tab`
    * **Simulate Visor Boop:** `B` key
    * *(Tip: If keyboard presses do not register in the emulator, open **Device Manager**, click the pencil/edit icon on your virtual device -> **Show Advanced Settings**, and make sure **Enable keyboard input** is checked.)*
* **Xbox Controller / Bluetooth Gamepad:** `PreviewActivity` natively supports Xbox and standard Android gamepads (`KeyEvent` and `MotionEvent` analog sticks, D-Pad hat axes, and triggers):
    * **Movement:** **D-Pad** or **Left Analog Stick**
    * **A Button:** **A**, **RB (R1)**, or **RT (Right Trigger)**
    * **B Button:** **B**, **X**, **Y**, **LB (L1)**, or **LT (Left Trigger)**
    * **Start Button:** **Menu / Start** button
    * **Select Button:** **View / Back / Select** button
    * **Simulate Visor Boop:** Click either analog stick (**L3** or **R3**)
    * *(Note on PC Emulator vs. Physical Phone with Xbox Controllers: When testing on a physical Android phone, any Bluetooth or USB-C Xbox controller connects natively to Android. When testing inside the PC Android Emulator on Windows, Android Studio's emulator does not forward XInput USB controllers directly by default—to use your Xbox controller on PC with the emulator, either keep Steam Desktop Input / JoyToKey / AntiMicroX running to map the controller to the keyboard keys above, or run `PreviewActivity` directly on your Android phone!)*
* **Simulating 3D Head Tilt (`getTiltX` / `getTiltY`):**
    1. Look at the vertical toolbar attached to the side of the Android Emulator window and click the **`...` (Extended Controls)** button at the bottom.
    2. Click **Virtual sensors** in the left menu of the popup window.
    3. Select the **Rotate** or **Move** radio button and drag the 3D phone model to tilt the virtual device in real time!

> 💡 **Testing a New or Renamed Game Class in the Emulator:** By default, `PreviewActivity.java` instantiates `new SampleGame()` (`game = new SampleGame();`). If you rename `SampleGame` or create a second game class (such as `MyCoolGame`), update that single line in `PreviewActivity.java` to `game = new MyCoolGame();` so the emulator previews your new class!

### Optional: Live Testing on a Physical Android Phone

If you prefer to run the live preview on your real Android phone (for example, to test directly with a Bluetooth Xbox controller or physical accelerometer):

1. On your phone, open **Settings -> About phone** and tap **Build number** 7 times until it says *"You are now a developer!"*.

2. Go back to **Settings -> System -> Developer options** and turn on **USB debugging** (to connect via USB cable) or **Wireless debugging** (to pair over Wi-Fi using **Pair device with QR code** inside Android Studio's **Device Manager**).

3. Select your physical phone in Android Studio's top device dropdown and click the green **Run ▶** button!

## ✍️ Writing, Packaging & Importing Your Game

### 1. Edit Your Game Code

1. In the left **Project** panel (in **Android** view), expand `app -> java -> com.protostream.devkit` and double-click **`SampleGame`**.

2. Update the `@GameInfo` annotation at the top with your game's `title`, `author`, and `description`:

   ```java
   @GameInfo(
       title = "My Visor Game",
       author = "YourName",
       description = "Describe your game and controls here."
   )
   ```

3. Write your initialization logic in `onInit()`, per-frame game logic in `onUpdate(Input input)`, and drawing code in `onRender(ProtoCanvas canvas)`.

> 💡 **How to Safely Rename `SampleGame`:** In Java, a `public class` name must match its `.java` filename. To rename `SampleGame`, right-click `SampleGame` in the left Project Explorer, select **Refactor -> Rename...** (`Shift + F6`), type your new class name, and press **Enter**. Android Studio will rename both the class and the file automatically.

### 2. Build Your `.protogame` Package

When you are ready to export your finished `.protogame` file for the ProtoStream app, you can use either the **Gradle Sidebar** or the **Terminal**:

#### Option A: Using the Gradle Sidebar

1. Click the **Gradle** tab on the **far-right sidebar** of Android Studio (the elephant icon).

2. Click the **Execute Gradle Task** icon at the top of the Gradle panel (or expand `ProtoStreamDevKit -> app -> Tasks -> protostream` and double-click **`buildProtoGame`**).

3. If using the popup bar, type `buildProtoGame` and press **Enter**.

#### Option B: Using the Bottom Terminal Tab

1. Click the **Terminal** tab at the **bottom** of Android Studio.

2. Run the following command and press **Enter**:
    * **Windows:** `.\gradlew.bat buildProtoGame`
    * **macOS / Linux:** `./gradlew buildProtoGame`

Android Studio will compile your code, generate `metadata.json`, bundle all sprite images from `app/src/main/assets/`, and print the output path in the build log!

### 3. Find Your `.protogame` File & Import to ProtoStream

1. In the left **Project** panel, click the dropdown at the top-left that says **Android** and switch it to **Project**.

2. Expand `ProtoStreamDevKit -> app -> output`.

3. Right-click your generated `<YourGameTitle>.protogame` file and select **Open In -> Explorer** (Windows) or **Open In -> Finder** (macOS).

4. Copy the `.protogame` file to your phone, open the **ProtoStream** app, go to **Games Plugin Manager -> Custom Games**, and tap **Import .protogame**!

## 🛠️ Complete API Reference

### 1. Game Lifecycle, Timing & Frame Counter

Every custom game extends `ProtoGame`. The engine executes your code inside a dedicated game loop running at approximately **30 FPS (~33ms per frame)**.

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

| Field | Type | Description |
| ----- | ----- | ----- |
| `title` | `String` | The display name of your game in the ProtoStream launcher. |
| `author` | `String` | Your name or developer handle. |
| `description` | `String` | A brief summary of gameplay or instructions. |

### 3. Display, Primitives & Fonts (`ProtoCanvas`)

The `ProtoCanvas` provides drawing primitives and sprite renderers mapped to the **128 x 32 pixel LED matrix**.

* **Coordinates:** `(x, y)` where `(0, 0)` is the **Top-Left** corner, and `(127, 31)` is the **Bottom-Right** corner.

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
| `clear(color)` | `int color` | Fills the entire `128 x 32` canvas with a solid ARGB color. |
| `setPixel(x, y, color)` | `int x, int y, int color` | Sets an individual pixel at `(x, y)` with alpha-blending support. |
| `fillRect(x, y, w, h, color)` | `int x, int y, int width, int height, int color` | Fills a solid rectangle from `(x, y)` extending `w` pixels right and `h` pixels down. |
| `drawRect(x, y, w, h, color)` | `int x, int y, int width, int height, int color` | Draws a 1-pixel unfilled rectangle outline. |
| `drawLine(x1, y1, x2, y2, color)` | `int x1, int y1, int x2, int y2, int color` | Draws a crisp 1-pixel line between `(x1, y1)` and `(x2, y2)`. |
| `drawCircle(cx, cy, r, color)` | `int cx, int cy, int radius, int color` | Draws a 1-pixel circle outline centered at `(cx, cy)`. |
| `fillCircle(cx, cy, r, color)` | `int cx, int cy, int radius, int color` | Draws a solid filled circle centered at `(cx, cy)`. |
| `drawText(text, x, y, color)` | `String text, int x, int y, int color` | Draws text using a bold 10pt monospace font (`y` = bottom baseline; use `y = 8..10` for top text). |
| `drawSmallText(text, x, y, color)` | `String text, int x, int y, int color` | Draws compact `3x5` pixel bitmap font text (`(x, y)` = **top-left** corner; 4px horizontal stride per character). |
| `drawImage(name, x, y)` | `String assetName, int x, int y` | Draws an image from `app/src/main/assets/` at top-left `(x, y)` with full alpha transparency. |
| `drawImageFlipped(...)` | `String assetName, int x, int y, boolean flipX, boolean flipY` | Draws an image from `app/src/main/assets/` with optional horizontal/vertical mirroring. |
| `drawSpriteFrame(...)` | `String assetName, int frameW, int frameH, int frameIndex, int x, int y, boolean flipX, boolean flipY` | Draws a single animation frame (`0, 1, 2, ...` left-to-right, top-to-bottom) from a sprite sheet in `assets/`. |
| `drawPixelMap(...)` | `int[] pixels, int w, int h, int x, int y, boolean flipX, boolean flipY` | Draws an in-code 1D ARGB pixel array of size `w * h` at `(x, y)` (`0x00000000` = transparent). |

### 4. Importing Sprites, Sprite Sheets & Pixel Maps

You do **not** need to draw every graphic manually with `setPixel` or `fillRect`! There are three easy ways to render sprites:

#### Method A: Drag-and-Drop Image Files (`app/src/main/assets/`)

1. Create your sprite or icon in any pixel-art editor (such as Aseprite, Piskel, LibreSprite, or Paint.NET) at visor scale (for example, `5x5`, `8x8`, or `16x16` pixels) and export it as a `.png` with a transparent background.

2. In Android Studio's left **Project** panel, locate **`app -> assets`** (or `app/src/main/assets` in **Project** view). Copy your `.png` file from Windows Explorer / Finder and paste it directly onto the `assets` folder in Android Studio!

3. Call `canvas.drawImage("heart.png", x, y)` or `canvas.drawImageFlipped("heart.png", x, y, flipX, flipY)` inside `onRender()`:

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

| Button Enum | Keyboard Mapping | Touch Overlay | Xbox / Gamepad Hardware Mapping |
| ----- | ----- | ----- | ----- |
| `Input.Button.UP` | `W` / Up Arrow | D-Pad Up | D-Pad Up / Left Stick Up |
| `Input.Button.DOWN` | `S` / Down Arrow | D-Pad Down | D-Pad Down / Left Stick Down |
| `Input.Button.LEFT` | `A` / Left Arrow | D-Pad Left | D-Pad Left / Left Stick Left |
| `Input.Button.RIGHT` | `D` / Right Arrow | D-Pad Right | D-Pad Right / Left Stick Right |
| `Input.Button.A` | `Z` / `Space` / `Enter` | A Button | Button A / Cross / RB (R1) / RT (Right Trigger) |
| `Input.Button.B` | `X` / `Shift` | B Button | Button B / Circle / X / Y / LB (L1) / LT (Left Trigger) |
| `Input.Button.START` | `P` / `Esc` | Start Button | Menu / Start Button |
| `Input.Button.SELECT` | `Tab` | Select Button | View / Back / Select Button |

> 💡 **Visor Boop & Head Gesture Support:** Any visor **Boop Detection** or **Head & Motion Gesture** configured in ProtoStream to emit a controller button or trigger input automatically maps to its corresponding `Input.Button` inside custom games! In `PreviewActivity`, you can also press `B` on your keyboard or click `L3` / `R3` on an Xbox controller to simulate a visor Boop.

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

    * Because the matrix is only `32` pixels tall, use `canvas.drawSmallText(...)` (`3x5` pixel font) for scores, lives, and status bars so your gameplay area has maximum vertical room.

3. **Optimized Screen Refreshing:**

    * Always call `canvas.clear(...)` at the start of `onRender()` to flush the frame buffer before drawing the next scene.

4. **Be Wary of Matrix Obstructions:**

    * Some external parts of the helmet may obstruct the visor (for example, in JTingF's models, the side fin rings block the `~20 x 20` pixel areas in the bottom-left and bottom-right corners). Keep vital HUD elements near the top or center of the visor.
