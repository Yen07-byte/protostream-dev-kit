# ProtoStream Game Dev Kit
> [License: PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

Notice: This project is source-available under the **PolyForm Noncommercial License 1.0.0**. You are free to modify, share, and use this Gradle plugin for any personal or non-commercial purpose. While you cannot sell the resulting `.protogame` files or use them for commercial exploitation, you are fully permitted to create files for both the free and paid versions of **Yen'sProtoStream**. Developers are also welcome to accept voluntary tips or donations for their creations on platforms like Patreon or Ko-fi, provided a fully functional, free version of the `.protogame` file remains publicly available.

---

Welcome to the ProtoStream Dev Kit! This template provides everything you need to write custom Java games for the ProtoStream Matrix platform.

## Quick Start Guide

### 1. Project Setup
You will need to clone this repository in **Android Studio**. If you haven't used Android Studio before or are unfamiliar with it, no worries! This guide will walk you through exactly what you need to do to get coding.
1. If you are currently in a project, click `File -> Close Project`. Then, in the top right corner, click **Clone Repository**, and paste the URL of this repository in the URL box (you can obtain this URL right here in GitHub by clicking the green **Code** button!). Then, click **Clone**.
2. Wait a moment for the initial Gradle Sync to finish loading the project dependencies.
3. You're all set up!

### 2. Write Your Game

1. Navigate to `app/src/main/java/com/protostream/devkit/` in the Project Explorer on the left side of your screen.
2. Open `SampleGame.java`. This is your main game file!
3. Fill out the `@GameInfo` annotation at the top with your game's Title (what is your game called?), Author (that's you!), and Description (what is your game about?).
4. Write your game logic inside `onUpdate()` and draw your pixels in `onRender()`.

*(Note: You can create as many helper Java classes as you want, just make sure your main class extends `ProtoGame`!)*

### 3. Build Your Game

When you are ready to test your game, you need to compile it into a `.protogame` file. 

The easiest and most reliable way to do this is using Android Studio's built-in task runner:
1. Open the **Gradle** tab on the right side of Android Studio.
2. In the top toolbar of the Gradle panel, click the **Execute Gradle Task** icon (it looks box with a play arrow icon in it, and is usually the second option from the left in this toolbar).
3. A "Run Anything" search bar will pop up. 
4. Type `buildProtoGame` and press **Enter**.

Android Studio will automatically compile your code, package your metadata, and build the file!

### 4. Import to ProtoStream

1. Look in your project folder on the left side of Android Studio. 
2. Expand the `app/output/` folder.
3. You will find your newly compiled `YourGameName.protogame` file here!
4. Transfer this file to your Android device, open the ProtoStream app, go to Plugin Manager -> Custom Games, and click **Import .protogame**!

## 🛠️ API Reference

### Input
Use the `Input` object passed into `onUpdate()` to check for controller presses.
```java
if (input.isPressed(Input.Button.A)) {
    // Jump!
}
```
Available Buttons: `UP`, `DOWN`, `LEFT`, `RIGHT`, `A`, `B`, `START`, `SELECT`.

### Canvas
Use the `ProtoCanvas` object passed into `onRender()` to draw to the 128x32 matrix.
```java
// Clear screen to black
canvas.clear(0xFF000000); 

// Draw a red pixel at x:10, y:15
canvas.setPixel(10, 15, 0xFFFF0000); 

// Draw a blue 5x5 rectangle at x:20, y:20
canvas.fillRect(20, 20, 5, 5, 0xFF0000FF); 

// Draw text (ProtoStream Green)
canvas.drawText("Hello World", 0, 10, 0xFF10B981);
```
*(Note: Colors must be provided as 32-bit ARGB Hex integers).*
