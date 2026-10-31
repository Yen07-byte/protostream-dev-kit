package yen.proto.stream.sdk

/*
 * ============================================================================
 * ProtoStream — ProtoStreamSDK.kt (yen.proto.stream.sdk)
 * ============================================================================
 * Overview:
 *   Public Software Development Kit (SDK) interfaces for building custom
 *   ".protogame" plugins that run inside ProtoStream's CustomGameHostActivity
 *   and the ProtoStream Dev Kit's built-in PreviewActivity.
 *
 * How to Create a Custom Game:
 *   1. Include this file (package yen.proto.stream.sdk) in your project.
 *   2. Place any custom PNG/GIF/BMP sprite images in app/src/main/assets/.
 *   3. Subclass ProtoGame and annotate your class with @GameInfo(title, author,
 *      description).
 *   4. Implement onInit(), onUpdate(input: Input), and onRender(canvas: ProtoCanvas).
 *      The host invokes onUpdate() and onRender() at 30 FPS on a 128x32 canvas.
 *   5. Run the Gradle task `buildProtoGame` to compile your code and bundle your
 *      assets + metadata.json into a ".protogame" archive.
 * ============================================================================
 */

/**
 * Annotation for contributors to declare their game's metadata directly within the source file.
 *
 * @property title Human-readable display title of the custom game.
 * @property author Creator or contributor name displayed in the Custom Games list.
 * @property description Short summary explaining gameplay and controls.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GameInfo(
    val title: String = "",
    val author: String = "",
    val description: String = ""
)

/**
 * Abstracts standard input methods and visor sensor hooks so contributors don't have to deal
 * with low-level Android KeyEvents or SensorManager callbacks. Automatically bridges on-screen
 * virtual buttons, physical Bluetooth/USB gamepads, keyboard keys, and simulated controller
 * inputs triggered by visor Boop Detection or Head & Motion Gestures.
 */
interface Input {
    /**
     * Standard 8-button virtual gamepad layout available to all custom visor games.
     */
    enum class Button {
        /** Directional pad Up button. */
        UP,

        /** Directional pad Down button. */
        DOWN,

        /** Directional pad Left button. */
        LEFT,

        /** Directional pad Right button. */
        RIGHT,

        /** Primary action button A. */
        A,

        /** Secondary action button B. */
        B,

        /** Start / pause button. */
        START,

        /** Select / mode button. */
        SELECT
    }

    /**
     * Queries whether the specified virtual or hardware [button] is currently held down
     * during the current 30 FPS update tick.
     *
     * @param button The [Button] constant to inspect.
     * @return True if the button is currently held down; false otherwise.
     */
    fun isPressed(button: Button): Boolean

    /**
     * Queries whether the specified [button] was pressed for the first time on this exact
     * frame (edge-triggered transition from unpressed to pressed). Ideal for single-fire
     * actions such as shooting, jumping, toggling modes, or pausing without manual debouncing.
     *
     * @param button The [Button] constant to inspect.
     * @return True only on the initial frame the button is pressed down.
     */
    fun isJustPressed(button: Button): Boolean = isPressed(button)

    /**
     * Queries whether the specified [button] was released on this exact frame
     * (edge-triggered transition from pressed to unpressed).
     *
     * @param button The [Button] constant to inspect.
     * @return True only on the frame the button is released.
     */
    fun isJustReleased(button: Button): Boolean = false

    /**
     * Returns true while a visor Boop event or active Boop overlay is currently occurring.
     *
     * @return True if the visor is currently booped; false otherwise.
     */
    fun isBooped(): Boolean = false

    /**
     * Returns true only on the initial frame a visor Boop event is triggered.
     *
     * @return True on the exact frame a new Boop occurs; false otherwise.
     */
    fun isJustBooped(): Boolean = false

    /**
     * Returns the normalized horizontal head/device roll tilt in the range `-1.0f..1.0f`
     * (negative = tilted left, positive = tilted right, `0.0f` = level).
     *
     * @return Normalized horizontal tilt value in `-1.0f..1.0f`.
     */
    fun getTiltX(): Float = 0f

    /**
     * Returns the normalized vertical head/device pitch tilt in the range `-1.0f..1.0f`
     * (negative = tilted up/back, positive = tilted down/forward, `0.0f` = level).
     *
     * @return Normalized vertical tilt value in `-1.0f..1.0f`.
     */
    fun getTiltY(): Float = 0f

    /**
     * Returns the normalized ambient/voice microphone amplitude in the range `0.0f..1.0f`
     * when microphone permission is granted (`0.0f` when silent or unavailable).
     *
     * @return Normalized audio level in `0.0f..1.0f`.
     */
    fun getMicLevel(): Float = 0f
}

/**
 * A 2D drawing canvas locked to the 128x32 HUB75 matrix dimensions.
 * Coordinates range from `x in 0..127` and `y in 0..31`, with `(0, 0)` at the top-left.
 */
interface ProtoCanvas {
    /**
     * Fills the entire 128x32 visor canvas with a solid 32-bit ARGB [color].
     *
     * @param color 32-bit ARGB color integer (e.g., `0xFF000000.toInt()` for black).
     */
    fun clear(color: Int)

    /**
     * Draws a text string onto the visor canvas using a bold 10pt monospace font.
     * Note that [y] specifies the bottom text baseline (use `y in 8..10` for top-row text).
     *
     * @param text Text string to render.
     * @param x Horizontal starting coordinate (`0..127`).
     * @param y Vertical baseline coordinate (`0..31`).
     * @param color 32-bit ARGB color integer.
     */
    fun drawText(text: String, x: Int, y: Int, color: Int)

    /**
     * Draws ultra-compact 3x5 pixel bitmap font text onto the visor canvas.
     * Unlike [drawText], [x] and [y] specify the top-left pixel corner of the text, and each
     * character advances 4 pixels horizontally (3px glyph + 1px spacing).
     *
     * @param text Text string to render in the 3x5 pixel font.
     * @param x Left X coordinate (`0..127`).
     * @param y Top Y coordinate (`0..31`).
     * @param color 32-bit ARGB color integer.
     */
    fun drawSmallText(text: String, x: Int, y: Int, color: Int) {}

    /**
     * Draws a filled axis-aligned rectangle onto the 128x32 visor canvas.
     *
     * @param x Left X coordinate (`0..127`).
     * @param y Top Y coordinate (`0..31`).
     * @param width Rectangle width in pixels.
     * @param height Rectangle height in pixels.
     * @param color 32-bit ARGB fill color integer.
     */
    fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Int)

    /**
     * Draws a 1-pixel unfilled rectangle outline onto the 128x32 visor canvas.
     *
     * @param x Left X coordinate (`0..127`).
     * @param y Top Y coordinate (`0..31`).
     * @param width Rectangle width in pixels.
     * @param height Rectangle height in pixels.
     * @param color 32-bit ARGB stroke color integer.
     */
    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int) {}

    /**
     * Draws a 1-pixel line segment between `(x1, y1)` and `(x2, y2)`.
     *
     * @param x1 Starting X coordinate.
     * @param y1 Starting Y coordinate.
     * @param x2 Ending X coordinate.
     * @param y2 Ending Y coordinate.
     * @param color 32-bit ARGB line color integer.
     */
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {}

    /**
     * Draws a 1-pixel circle outline centered at `(cx, cy)` with the given [radius].
     *
     * @param cx Center X coordinate.
     * @param cy Center Y coordinate.
     * @param radius Circle radius in pixels.
     * @param color 32-bit ARGB stroke color integer.
     */
    fun drawCircle(cx: Int, cy: Int, radius: Int, color: Int) {}

    /**
     * Draws a solid filled circle centered at `(cx, cy)` with the given [radius].
     *
     * @param cx Center X coordinate.
     * @param cy Center Y coordinate.
     * @param radius Circle radius in pixels.
     * @param color 32-bit ARGB fill color integer.
     */
    fun fillCircle(cx: Int, cy: Int, radius: Int, color: Int) {}

    /**
     * Sets a single pixel at `(x, y)` on the 128x32 visor canvas to [color].
     * Out-of-bounds coordinates outside `0..127` or `0..31` are safely ignored by the host.
     *
     * @param x Horizontal pixel coordinate (`0..127`).
     * @param y Vertical pixel coordinate (`0..31`).
     * @param color 32-bit ARGB pixel color integer.
     */
    fun setPixel(x: Int, y: Int, color: Int)

    /**
     * Draws a bundled image asset from `app/src/main/assets/` at top-left coordinates `(x, y)`
     * with full alpha transparency support.
     *
     * @param assetName Filename or relative path inside `assets/` (e.g., `"ship.png"`).
     * @param x Destination left X coordinate (`0..127`).
     * @param y Destination top Y coordinate (`0..31`).
     */
    fun drawImage(assetName: String, x: Int, y: Int) {}

    /**
     * Draws a bundled image asset from `app/src/main/assets/` at `(x, y)` with optional
     * horizontal and/or vertical mirroring.
     *
     * @param assetName Filename or relative path inside `assets/` (e.g., `"ship.png"`).
     * @param x Destination left X coordinate (`0..127`).
     * @param y Destination top Y coordinate (`0..31`).
     * @param flipX True to mirror the sprite horizontally.
     * @param flipY True to mirror the sprite vertically.
     */
    fun drawImageFlipped(
        assetName: String,
        x: Int,
        y: Int,
        flipX: Boolean,
        flipY: Boolean
    ) {}

    /**
     * Draws a single animation frame from a grid or strip sprite-sheet image in `assets/`.
     * Frames are indexed `0, 1, 2, ...` from left to right, top to bottom.
     *
     * @param assetName Filename or relative path inside `assets/` (e.g., `"player_sheet.png"`).
     * @param frameWidth Width of a single sprite frame in pixels.
     * @param frameHeight Height of a single sprite frame in pixels.
     * @param frameIndex Zero-based frame index into the sprite sheet.
     * @param x Destination left X coordinate (`0..127`).
     * @param y Destination top Y coordinate (`0..31`).
     * @param flipX True to mirror the frame horizontally.
     * @param flipY True to mirror the frame vertically.
     */
    fun drawSpriteFrame(
        assetName: String,
        frameWidth: Int,
        frameHeight: Int,
        frameIndex: Int,
        x: Int,
        y: Int,
        flipX: Boolean,
        flipY: Boolean
    ) {}

    /**
     * Draws an in-code 1D ARGB pixel array of dimensions `width x height` at `(x, y)`.
     * Pixels with an alpha channel of `0` (`0x00000000`) are treated as transparent.
     *
     * @param pixels 1D array of 32-bit ARGB colors of length at least `width * height`.
     * @param width Sprite width in pixels.
     * @param height Sprite height in pixels.
     * @param x Destination left X coordinate (`0..127`).
     * @param y Destination top Y coordinate (`0..31`).
     * @param flipX True to mirror the pixel map horizontally.
     * @param flipY True to mirror the pixel map vertically.
     */
    fun drawPixelMap(
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        flipX: Boolean,
        flipY: Boolean
    ) {}
}

/**
 * The base class all custom `.protogame` plugins must extend.
 * Ensures `CustomGameHostActivity` can manage the 30 FPS game loop lifecycle and provide
 * sandboxed key-value persistence for high scores and game settings.
 */
abstract class ProtoGame {
    /**
     * Internal bridge interface implemented by the host activity to persist per-game save data.
     */
    interface StorageBridge {
        /**
         * Persists an integer [value] under [key] in the game's sandboxed storage.
         *
         * @param key Lookup key string.
         * @param value Integer value to store.
         */
        fun saveInt(key: String, value: Int)

        /**
         * Loads a persisted integer for [key], or returns [defaultValue] if not found.
         *
         * @param key Lookup key string.
         * @param defaultValue Fallback value when the key does not exist.
         * @return Stored integer value or [defaultValue].
         */
        fun loadInt(key: String, defaultValue: Int): Int

        /**
         * Persists a string [value] under [key] in the game's sandboxed storage.
         *
         * @param key Lookup key string.
         * @param value String value to store.
         */
        fun saveString(key: String, value: String)

        /**
         * Loads a persisted string for [key], or returns [defaultValue] if not found.
         *
         * @param key Lookup key string.
         * @param defaultValue Fallback string when the key does not exist.
         * @return Stored string value or [defaultValue].
         */
        fun loadString(key: String, defaultValue: String): String
    }

    private var storageBridge: StorageBridge? = null
    private var frameCounter: Long = 0L

    /**
     * Attaches the host's sandboxed storage bridge prior to [onInit].
     *
     * @param bridge Host-provided [StorageBridge] implementation.
     */
    fun attachStorageBridge(bridge: StorageBridge) {
        storageBridge = bridge
    }

    /**
     * Advances the internal frame tick counter by one frame. Called automatically by the host
     * prior to each [onUpdate] invocation.
     */
    fun incrementFrameCount() {
        frameCounter++
    }

    /**
     * Returns the number of 30 FPS update ticks elapsed since the game started.
     * Useful for driving sprite animations (`(int)(getFrameCount() / 4) % numFrames`) and timers.
     *
     * @return Elapsed frame tick count.
     */
    fun getFrameCount(): Long = frameCounter

    /**
     * Saves an integer value (such as a high score or unlocked level) to persistent storage.
     *
     * @param key Unique key name (e.g., `"highScore"`).
     * @param value Integer value to persist.
     */
    fun saveInt(key: String, value: Int) {
        storageBridge?.saveInt(key, value)
    }

    /**
     * Loads a previously saved integer value from persistent storage.
     *
     * @param key Unique key name (e.g., `"highScore"`).
     * @param defaultValue Value returned if [key] has never been saved.
     * @return The saved integer, or [defaultValue].
     */
    fun loadInt(key: String, defaultValue: Int): Int {
        return storageBridge?.loadInt(key, defaultValue) ?: defaultValue
    }

    /**
     * Saves a string value (such as a player initials tag or serialized state) to storage.
     *
     * @param key Unique key name.
     * @param value String value to persist.
     */
    fun saveString(key: String, value: String) {
        storageBridge?.saveString(key, value)
    }

    /**
     * Loads a previously saved string value from persistent storage.
     *
     * @param key Unique key name.
     * @param defaultValue Value returned if [key] has never been saved.
     * @return The saved string, or [defaultValue].
     */
    fun loadString(key: String, defaultValue: String): String {
        return storageBridge?.loadString(key, defaultValue) ?: defaultValue
    }

    /**
     * Called once when the game plugin is first loaded by the host activity.
     * Initialize all game state, object pools, and saved high scores here.
     */
    abstract fun onInit()

    /**
     * Called once per frame (~30 FPS) prior to [onRender] to advance game physics and logic.
     *
     * @param input Current frame input state for polling buttons and visor sensors.
     */
    abstract fun onUpdate(input: Input)

    /**
     * Called once per frame (~30 FPS) immediately after [onUpdate] to draw the current
     * game frame onto the 128x32 visor [canvas].
     *
     * @param canvas Offscreen 128x32 visor canvas transmitted over UDP to the ESP32.
     */
    abstract fun onRender(canvas: ProtoCanvas)
}