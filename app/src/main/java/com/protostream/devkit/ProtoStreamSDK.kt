package yen.proto.stream.sdk

/*
 * ============================================================================
 * ProtoStream — ProtoStreamSDK.kt (yen.proto.stream.sdk)
 * ============================================================================
 * Overview:
 *   Public Software Development Kit (SDK) interfaces for building custom
 *   ".protogame" plugins that run inside ProtoStream's CustomGameHostActivity.
 *
 * How to Create a Custom Game:
 *   1. Include this file (package yen.proto.stream.sdk) in your project as a
 *      compileOnly dependency.
 *   2. Subclass ProtoGame and annotate your class with @GameInfo(title, author,
 *      description).
 *   3. Implement onInit(), onUpdate(input: Input), and onRender(canvas: ProtoCanvas).
 *      The host invokes onUpdate() and onRender() at 30 FPS on a 128x32 canvas.
 *   4. Compile your game class to Dalvik bytecode (classes.dex) and package it
 *      with a metadata.json file into a ".protogame" ZIP archive.
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
 * Abstracts standard input methods so contributors don't have to deal with Android KeyEvents.
 * Automatically bridges on-screen virtual buttons, physical Bluetooth/USB gamepads, and
 * simulated controller inputs triggered by visor Boop Detection or Head Gestures.
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
     * @return True if the button is currently pressed; false otherwise.
     */
    fun isPressed(button: Button): Boolean
}

/**
 * A simplified 2D drawing canvas locked to the 128x32 HUB75 matrix dimensions.
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
     * Draws a text string onto the visor canvas at the specified top-left coordinates.
     *
     * @param text Text string to render.
     * @param x Horizontal starting coordinate (`0..127`).
     * @param y Vertical baseline/top coordinate (`0..31`).
     * @param color 32-bit ARGB color integer.
     */
    fun drawText(text: String, x: Int, y: Int, color: Int)

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
     * Sets a single pixel at `(x, y)` on the 128x32 visor canvas to [color].
     * Out-of-bounds coordinates outside `0..127` or `0..31` are safely ignored by the host.
     *
     * @param x Horizontal pixel coordinate (`0..127`).
     * @param y Vertical pixel coordinate (`0..31`).
     * @param color 32-bit ARGB pixel color integer.
     */
    fun setPixel(x: Int, y: Int, color: Int)
}

/**
 * The base class all custom `.protogame` plugins must extend.
 * Ensures `CustomGameHostActivity` can manage the 30 FPS game loop lifecycle safely.
 */
abstract class ProtoGame {
    /**
     * Called once when the game plugin is first loaded or reset by the host activity.
     * Initialize all game state, entities, and scores here.
     */
    abstract fun onInit()

    /**
     * Called once per frame (~30 FPS) prior to [onRender] to advance game physics and logic.
     *
     * @param input Current frame input state for polling button presses.
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