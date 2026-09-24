package yen.proto.stream.sdk

/**
 * Annotation for contributors to declare their game's metadata directly within the Java file.
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
 */
interface Input {
    enum class Button { 
        UP, DOWN, LEFT, RIGHT, A, B, START, SELECT 
    }
    
    fun isPressed(button: Button): Boolean
}

/**
 * A simplified canvas locked to the ProtoStream 128x32 constraints.
 */
interface ProtoCanvas {
    fun clear(color: Int)
    fun drawText(text: String, x: Int, y: Int, color: Int)
    fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Int)
    fun setPixel(x: Int, y: Int, color: Int)
}

/**
 * The base class all custom games must extend.
 */
abstract class ProtoGame {
    abstract fun onInit()
    abstract fun onUpdate(input: Input)
    abstract fun onRender(canvas: ProtoCanvas)
}