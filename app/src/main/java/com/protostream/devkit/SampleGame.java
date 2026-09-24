package com.protostream.devkit;

import yen.proto.stream.sdk.ProtoGame;
import yen.proto.stream.sdk.ProtoCanvas;
import yen.proto.stream.sdk.Input;
import yen.proto.stream.sdk.GameInfo;

@GameInfo(
        title = "Space Defender",
        author = "ProtoStream Devs",
        description = "Full 128x32 arcade game showcasing physical controller support, starfields, particles, and state management."
)
public class SampleGame extends ProtoGame {

    // --- GAME STATES ---
    private enum State { PLAYING, PAUSED, GAME_OVER }
    private State currentState = State.PLAYING;

    // --- PLAYER PROPERTIES ---
    private float playerX = 10;
    private float playerY = 14;
    private int playerHealth = 100;
    private int score = 0;

    // --- ENTITY POOLS ---
    private static class Bullet { float x, y; boolean active; }
    private static class Enemy { float x, y, speed; int type; boolean active; }
    private static class Particle { float x, y, vx, vy; int color; int life; }
    private static class Star { float x, y, speed; int brightness; }

    private final Bullet[] bullets = new Bullet[10];
    private final Enemy[] enemies = new Enemy[8];
    private final Particle[] particles = new Particle[30];
    private final Star[] stars = new Star[20];

    // Input Debouncing
    private boolean lastStartPressed = false;
    private boolean lastAPressed = false;

    @Override
    public void onInit() {
        resetGame();

        for (int i = 0; i < bullets.length; i++) bullets[i] = new Bullet();
        for (int i = 0; i < enemies.length; i++) enemies[i] = new Enemy();
        for (int i = 0; i < particles.length; i++) particles[i] = new Particle();

        // Generate background starfield
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star();
            stars[i].x = (float) (Math.random() * 128);
            stars[i].y = (float) (Math.random() * 32);
            stars[i].speed = 0.2f + (float) Math.random() * 0.8f;
            stars[i].brightness = 0xFF555555 + ((int)(Math.random() * 170) << 16 | (int)(Math.random() * 170) << 8 | (int)(Math.random() * 170));
        }
    }

    private void resetGame() {
        playerX = 10;
        playerY = 14;
        playerHealth = 100;
        score = 0;
        currentState = State.PLAYING;

        if (enemies != null) for (Enemy e : enemies) if (e != null) e.active = false;
        if (bullets != null) for (Bullet b : bullets) if (b != null) b.active = false;
    }

    @Override
    public void onUpdate(Input input) {
        // --- CONTROLLER SUPPORT DEMO: START BUTTON PAUSE / RESUME ---
        boolean startPressed = input.isPressed(Input.Button.START);
        if (startPressed && !lastStartPressed) {
            if (currentState == State.PLAYING) currentState = State.PAUSED;
            else if (currentState == State.PAUSED) currentState = State.PLAYING;
            else if (currentState == State.GAME_OVER) resetGame();
        }
        lastStartPressed = startPressed;

        if (currentState != State.PLAYING) return;

        // --- PLAYER MOVEMENT (Keyboard, Touch D-Pad, or Gamepad Analog Stick) ---
        float speed = input.isPressed(Input.Button.B) ? 2.2f : 1.2f; // Hold B button for Turbo Boost!
        if (input.isPressed(Input.Button.LEFT) && playerX > 2) playerX -= speed;
        if (input.isPressed(Input.Button.RIGHT) && playerX < 80) playerX += speed;
        if (input.isPressed(Input.Button.UP) && playerY > 2) playerY -= speed;
        if (input.isPressed(Input.Button.DOWN) && playerY < 26) playerY += speed;

        // --- A BUTTON LASER SHOOTING ---
        boolean aPressed = input.isPressed(Input.Button.A);
        if (aPressed && !lastAPressed) spawnBullet();
        lastAPressed = aPressed;

        // Parallax Starfield movement
        for (Star star : stars) {
            star.x -= star.speed;
            if (star.x < 0) { star.x = 127; star.y = (float) (Math.random() * 32); }
        }

        // Bullets physics
        for (Bullet b : bullets) {
            if (b.active) {
                b.x += 3f;
                if (b.x > 128) b.active = false;
            }
        }

        // Enemy Waves & Collisions
        for (Enemy e : enemies) {
            if (e.active) {
                e.x -= e.speed;
                if (e.x < -6) e.active = false;

                // Bullet hits Enemy
                for (Bullet b : bullets) {
                    if (b.active && Math.abs(b.x - e.x) < 5 && Math.abs(b.y - e.y) < 5) {
                        b.active = false;
                        e.active = false;
                        score += (e.type == 0) ? 10 : 25;
                        spawnExplosion(e.x, e.y);
                    }
                }

                // Enemy collides with Player
                if (e.active && Math.abs(e.x - playerX) < 6 && Math.abs(e.y - playerY) < 6) {
                    e.active = false;
                    playerHealth -= 25;
                    spawnExplosion(playerX, playerY);
                    if (playerHealth <= 0) {
                        playerHealth = 0;
                        currentState = State.GAME_OVER;
                    }
                }
            } else if (Math.random() < 0.03) {
                e.active = true;
                e.x = 128;
                e.y = (float) (2 + Math.random() * 24);
                e.type = Math.random() > 0.6 ? 1 : 0;
                e.speed = e.type == 1 ? 1.4f : 0.8f;
            }
        }

        // Particle System Decay
        for (Particle p : particles) {
            if (p.life > 0) {
                p.x += p.vx;
                p.y += p.vy;
                p.life--;
            }
        }
    }

    private void spawnBullet() {
        for (Bullet b : bullets) {
            if (!b.active) {
                b.x = playerX + 6;
                b.y = playerY + 2;
                b.active = true;
                break;
            }
        }
    }

    private void spawnExplosion(float x, float y) {
        for (Particle p : particles) {
            p.x = x;
            p.y = y;
            p.vx = (float) (Math.random() * 2 - 1);
            p.vy = (float) (Math.random() * 2 - 1);
            p.life = 10 + (int) (Math.random() * 10);
            p.color = Math.random() > 0.5 ? 0xFFEF4444 : 0xFFF59E0B;
        }
    }

    @Override
    public void onRender(ProtoCanvas canvas) {
        canvas.clear(0xFF0A0A10); // Deep space background

        // 1. Starfield Background
        for (Star star : stars) canvas.setPixel((int) star.x, (int) star.y, star.brightness);

        // State Screens
        if (currentState == State.PAUSED) {
            canvas.drawText("PAUSED", 46, 12, 0xFFF59E0B);
            canvas.drawText("Press START", 34, 24, 0xFF9CA3AF);
            return;
        }
        if (currentState == State.GAME_OVER) {
            canvas.drawText("GAME OVER", 38, 10, 0xFFEF4444);
            canvas.drawText("SCORE: " + score, 42, 20, 0xFF10B981);
            canvas.drawText("Press START", 34, 30, 0xFF9CA3AF);
            return;
        }

        // 2. Explosions
        for (Particle p : particles) if (p.life > 0) canvas.setPixel((int) p.x, (int) p.y, p.color);

        // 3. Player Ship (Custom blocks)
        int px = (int) playerX, py = (int) playerY;
        canvas.fillRect(px, py + 1, 6, 3, 0xFF3B82F6);
        canvas.fillRect(px + 4, py + 2, 3, 1, 0xFF60A5FA);
        canvas.setPixel(px - 1, py + 2, 0xFFEF4444); // Thruster tail

        // 4. Lasers
        for (Bullet b : bullets) if (b.active) canvas.fillRect((int) b.x, (int) b.y, 2, 1, 0xFF34D399);

        // 5. Enemies
        for (Enemy e : enemies) {
            if (e.active) {
                int ex = (int) e.x, ey = (int) e.y;
                if (e.type == 0) canvas.fillRect(ex, ey, 4, 4, 0xFF6B7280); // Asteroid
                else {
                    canvas.fillRect(ex, ey + 1, 5, 3, 0xFFA855F7); // Alien Fighter
                    canvas.setPixel(ex - 1, ey + 2, 0xFFEC4899);
                }
            }
        }

        // 6. HUD - Health bar & Live Score
        int healthWidth = (playerHealth * 20) / 100;
        canvas.fillRect(2, 2, 22, 4, 0xFF374151);
        if (healthWidth > 0) {
            canvas.fillRect(3, 3, healthWidth, 2, playerHealth > 40 ? 0xFF10B981 : 0xFFEF4444);
        }
        canvas.drawText("S:" + score, 85, 8, 0xFFF59E0B);
    }
}