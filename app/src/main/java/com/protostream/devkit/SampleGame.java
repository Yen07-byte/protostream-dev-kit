package com.protostream.devkit;

import yen.proto.stream.sdk.ProtoGame;
import yen.proto.stream.sdk.ProtoCanvas;
import yen.proto.stream.sdk.Input;
import yen.proto.stream.sdk.GameInfo;

@GameInfo(
    title = "Pixel Bouncer",
    author = "Your Name",
    description = "A simple bouncing square demo for ProtoStream."
)
public class SampleGame extends ProtoGame {

    private int posX = 10;
    private int posY = 10;
    private int velX = 1;
    private int velY = 1;

    private final int boxWidth = 6;
    private final int boxHeight = 6;

    @Override
    public void onInit() {
        posX = 10;
        posY = 10;
    }

    @Override
    public void onUpdate(Input input) {
        // Player manual override controls
        if (input.isPressed(Input.Button.LEFT)) posX--;
        if (input.isPressed(Input.Button.RIGHT)) posX++;
        if (input.isPressed(Input.Button.UP)) posY--;
        if (input.isPressed(Input.Button.DOWN)) posY++;

        // Automatic bounce logic
        posX += velX;
        posY += velY;

        if (posX <= 0 || posX + boxWidth >= 128) velX *= -1;
        if (posY <= 0 || posY + boxHeight >= 32) velY *= -1;
    }

    @Override
    public void onRender(ProtoCanvas canvas) {
        // Dark background
        canvas.clear(0xFF111118);

        // Draw HUD / title text
        canvas.drawText("BOUNCER", 2, 8, 0xFF60A5FA);

        // Draw bouncing player box (ProtoStream Green)
        canvas.fillRect(posX, posY, boxWidth, boxHeight, 0xFF10B981);
    }
}