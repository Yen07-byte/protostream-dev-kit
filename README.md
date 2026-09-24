# ProtoStream Game Dev Kit
> [License: PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

Notice: This project is source-available under the **PolyForm Noncommercial License 1.0.0**. You are free to modify, share, and use this Gradle plugin for any personal or non-commercial purpose. While you cannot sell the resulting `.protogame` files or use them for commercial exploitation, you are fully permitted to create files for both the free and paid versions of **Yen'sProtoStream**. Developers are also welcome to accept voluntary tips or donations for their creations on platforms like Patreon or Ko-fi, provided a fully functional, free version of the `.protogame` file remains publicly available.

---

Welcome to the official **ProtoStream Dev Kit**! This repository contains everything you need to build custom 128x32 pixel, Java-coded games for the **ProtoStream** Android app without needing to learn complex Android UI development.

---

## How to Get Started

### 1. Clone this Repository
To use this Dev Kit, you must open it as an existing project in Android Studio.

1. Open Android Studio.
2. Click Get from VCS (or File -> New -> Project from Version Control).
3. Paste the URL of this repository and click Clone.
4. Important: Wait a moment for the bottom progress bar to finish "Gradle Sync". If you see a banner at the top saying "Code insight unavailable" or "Sync Now", click Sync Now to download the required build tools.

### 2. Write Your Game
1. Navigate to `src/main/java/com/protostream/devkit/`.
2. Edit `SampleGame.java` (or create a new Java class extending `ProtoGame`).
3. Fill out the `@GameInfo` annotation at the top of your class:

```java
@GameInfo(
    title = "My Cool Game",
    author = "Your Name",
    description = "An awesome game!"
)
public class MyCoolGame extends ProtoGame {
    // ... your game code here
}
```

---

## How the ProtoStream Engine Works

You don't need to touch Android `View`s, `Activity` lifecycles, or canvas rendering math!

### **Game Loop Methods**
- `onInit()`: Runs once when your game is launched. Use this to set initial player positions or scores.
- `onUpdate(Input input)`: Runs ~30 times per second. Check controller inputs and update game state here.
- `onRender(ProtoCanvas canvas)`: Runs every frame. Draw pixels, rectangles, or text.

### **Canvas & Screen Bounds**
- The screen resolution is strictly **128 x 32 pixels**.
- **NOTE FOR THOSE USING JTingF's PRINTS:** The side fin rings block the bottom left and bottom right corners in a roughly 20x20 pixel area. I strongly recommend avoiding these areas where possible!
- Colors use standard ARGB hex integers (e.g., `0xFF10B981` for green, `0xFFFFFFFF` for white).

### **Input Buttons**
Check for button holds using `input.isPressed(Input.Button.<BUTTON>)`:
- `Input.Button.UP`
- `Input.Button.DOWN`
- `Input.Button.LEFT`
- `Input.Button.RIGHT`
- `Input.Button.A`
- `Input.Button.B`

---

## Building Your `.protogame` File

When you are ready to test your game on the ProtoStream app:

1. Open the **Gradle** side panel in Android Studio (or use the terminal).
2. Run the task under `Tasks -> protostream -> buildProtoGame`.
   - Or run `./gradlew buildProtoGame` in the terminal.
3. Your compiled `.protogame` file will be created inside the project's `output/` directory!

---

## Installing on Your Device

1. Transfer the generated `.protogame` file to your Android device.
2. Open **ProtoStream** -> Go to **Games Plugin Manager** -> Tap **Custom Games**.
3. Tap **Import .protogame** and pick your file!
4. Tap **Launch** to play!