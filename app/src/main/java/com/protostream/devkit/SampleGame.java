package com.protostream.devkit;

import yen.proto.stream.sdk.GameInfo;
import yen.proto.stream.sdk.Input;
import yen.proto.stream.sdk.ProtoCanvas;
import yen.proto.stream.sdk.ProtoGame;

/**
 * Showcase 128x32 arcade game demonstrating every feature of the ProtoStream SDK:
 * - PNG Sprite & Animated Sprite-Sheet rendering ("player_sheet.png", "enemy_sheet.png",
 *   "coin_sheet.png", "heart.png") from app/src/main/assets/
 * - In-code 1D ARGB pixel maps (drawPixelMap)
 * - Edge-triggered button debouncing (input.isJustPressed)
 * - On-screen & hardware START (pause/restart) and SELECT (weapon mode toggle) buttons
 * - Extended geometric primitives (drawRect, drawLine, drawCircle, fillCircle, drawSmallText)
 * - Direct visor sensor integration (input.isJustBooped EMP blast & input.getTiltY steering)
 * - Persistent high-score storage (loadInt & saveInt) and frame tick counter (getFrameCount)
 */
@GameInfo(
        title = "Space Defender DX",
        author = "ProtoStream Devs",
        description = "Full 128x32 showcase game featuring PNG sprite sheets, SELECT weapon switching, START pause menu, Boop EMP blasts, and saved high scores."
)
public class SampleGame extends ProtoGame {

    // --- GAME STATES & WEAPON MODES ---
    private enum State { PLAYING, PAUSED, GAME_OVER }
    private State currentState = State.PLAYING;
    private int weaponMode = 0; // 0 = Twin Laser, 1 = Wave Beam

    // --- PLAYER PROPERTIES ---
    private float playerX = 10f;
    private float playerY = 14f;
    private int playerLives = 3;
    private int score = 0;
    private int highScore = 0;
    private int empShockwaveRadius = -1;
    private int empOriginX = 64;
    private int empOriginY = 16;

    // --- IN-CODE PIXEL MAP DEMO (5x5 Cyan Shield Badge) ---
    private static final int C = 0xFF38BDF8;
    private static final int W = 0xFFE0F2FE;
    private static final int _ = 0x00000000;
    private static final int[] SHIELD_ICON_PIXELS = new int[] {
            _, C, C, C, _,
            C, W, C, W, C,
            C, C, W, C, C,
            _, C, C, C, _,
            _, _, C, _, _
    };

    // --- ENTITY POOLS (Pre-allocated in onInit to avoid GC stutter) ---
    private static class Bullet {
        float x, y, vy;
        boolean isWave;
        boolean active;
    }

    private static class Enemy {
        float x, y, speed;
        int type;
        boolean active;
    }

    private static class Coin {
        float x, y;
        boolean active;
    }

    private static class Particle {
        float x, y, vx, vy;
        int color;
        int life;
    }

    private static class Star {
        float x, y, speed;
        int brightness;
    }

    private final Bullet[] bullets = new Bullet[14];
    private final Enemy[] enemies = new Enemy[8];
    private final Coin[] coins = new Coin[4];
    private final Particle[] particles = new Particle[30];
    private final Star[] stars = new Star[20];

    @Override
    public void onInit() {
        // Load persisted high score from sandboxed game storage
        highScore = loadInt("highScore", 0);

        for (int i = 0; i < bullets.length; i++) bullets[i] = new Bullet();
        for (int i = 0; i < enemies.length; i++) enemies[i] = new Enemy();
        for (int i = 0; i < coins.length; i++) coins[i] = new Coin();
        for (int i = 0; i < particles.length; i++) particles[i] = new Particle();

        // Generate parallax background starfield
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star();
            stars[i].x = (float) (Math.random() * 128);
            stars[i].y = (float) (7 + Math.random() * 24);
            stars[i].speed = 0.2f + (float) Math.random() * 0.8f;
            int shade = 80 + (int) (Math.random() * 150);
            stars[i].brightness = 0xFF000000 | (shade << 16) | (shade << 8) | shade;
        }

        resetGame();
    }

    private void resetGame() {
        playerX = 10f;
        playerY = 16f;
        playerLives = 3;
        score = 0;
        empShockwaveRadius = -1;
        currentState = State.PLAYING;

        for (Enemy e : enemies) if (e != null) e.active = false;
        for (Bullet b : bullets) if (b != null) b.active = false;
        for (Coin c : coins) if (c != null) c.active = false;
    }

    @Override
    public void onUpdate(Input input) {
        // --- 1. START BUTTON: PAUSE / RESUME / RESTART (Using built-in isJustPressed!) ---
        if (input.isJustPressed(Input.Button.START)) {
            if (currentState == State.PLAYING) {
                currentState = State.PAUSED;
            } else if (currentState == State.PAUSED) {
                currentState = State.PLAYING;
            } else if (currentState == State.GAME_OVER) {
                resetGame();
            }
        }

        // --- 2. SELECT BUTTON: SWITCH WEAPON MODE (TWIN LASER vs WAVE BEAM) ---
        if (input.isJustPressed(Input.Button.SELECT)) {
            weaponMode = (weaponMode + 1) % 2;
        }

        if (currentState != State.PLAYING) return;

        // --- 3. VISOR BOOP SENSOR: TRIGGER EMP SHOCKWAVE ---
        if (input.isJustBooped() && empShockwaveRadius < 0) {
            empOriginX = (int) playerX + 4;
            empOriginY = (int) playerY + 3;
            empShockwaveRadius = 2;
        }

        if (empShockwaveRadius >= 0) {
            empShockwaveRadius += 6;
            for (Enemy e : enemies) {
                if (e.active) {
                    float dx = e.x - empOriginX;
                    float dy = e.y - empOriginY;
                    if (Math.sqrt(dx * dx + dy * dy) <= empShockwaveRadius) {
                        e.active = false;
                        addScore(15);
                        spawnExplosion(e.x, e.y);
                    }
                }
            }
            if (empShockwaveRadius > 130) {
                empShockwaveRadius = -1;
            }
        }

        // --- 4. PLAYER MOVEMENT (D-Pad, Analog Stick, or Subtle Head Tilt) ---
        float speed = input.isPressed(Input.Button.B) ? 2.2f : 1.3f; // Hold B for Turbo Boost
        if (input.isPressed(Input.Button.LEFT) && playerX > 2) playerX -= speed;
        if (input.isPressed(Input.Button.RIGHT) && playerX < 84) playerX += speed;
        if (input.isPressed(Input.Button.UP) && playerY > 8) playerY -= speed;
        if (input.isPressed(Input.Button.DOWN) && playerY < 25) playerY += speed;

        // Optional subtle vertical tilt steering if UP/DOWN are not held
        if (!input.isPressed(Input.Button.UP) && !input.isPressed(Input.Button.DOWN)) {
            float tiltY = input.getTiltY();
            if (Math.abs(tiltY) > 0.18f) {
                playerY = Math.max(8f, Math.min(25f, playerY + tiltY * 1.1f));
            }
        }

        // --- 5. A BUTTON: FIRE SELECTED WEAPON ---
        if (input.isJustPressed(Input.Button.A)) {
            spawnWeaponFire();
        }

        // Parallax Starfield movement
        for (Star star : stars) {
            star.x -= star.speed;
            if (star.x < 0) {
                star.x = 127;
                star.y = (float) (7 + Math.random() * 24);
            }
        }

        // Bullets physics
        for (Bullet b : bullets) {
            if (b.active) {
                b.x += 3.2f;
                b.y += b.vy;
                if (b.x > 128 || b.y < 7 || b.y > 31) {
                    b.active = false;
                }
            }
        }

        // Collectible Energy Crystals
        for (Coin c : coins) {
            if (c.active) {
                c.x -= 0.9f;
                if (c.x < -5) c.active = false;
                if (Math.abs(c.x - playerX) < 6 && Math.abs(c.y - playerY) < 6) {
                    c.active = false;
                    addScore(50);
                }
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
                        addScore(e.type == 0 ? 10 : 25);
                        spawnExplosion(e.x, e.y);
                        if (Math.random() < 0.35) {
                            spawnCoin(e.x, e.y);
                        }
                    }
                }

                // Enemy collides with Player
                if (e.active && Math.abs(e.x - playerX) < 6 && Math.abs(e.y - playerY) < 5) {
                    e.active = false;
                    playerLives--;
                    spawnExplosion(playerX, playerY);
                    if (playerLives <= 0) {
                        playerLives = 0;
                        currentState = State.GAME_OVER;
                    }
                }
            } else if (Math.random() < 0.03) {
                e.active = true;
                e.x = 128;
                e.y = (float) (8 + Math.random() * 18);
                e.type = Math.random() > 0.55 ? 1 : 0;
                e.speed = e.type == 1 ? 1.4f : 0.85f;
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

    private void addScore(int points) {
        score += points;
        if (score > highScore) {
            highScore = score;
            saveInt("highScore", highScore);
        }
    }

    private void spawnWeaponFire() {
        if (weaponMode == 0) {
            // Twin Laser
            spawnSingleBullet(playerX + 7, playerY + 1, 0f, false);
            spawnSingleBullet(playerX + 7, playerY + 4, 0f, false);
        } else {
            // 3-Way Spread Wave
            spawnSingleBullet(playerX + 7, playerY + 2, -0.45f, true);
            spawnSingleBullet(playerX + 7, playerY + 2, 0f, true);
            spawnSingleBullet(playerX + 7, playerY + 2, 0.45f, true);
        }
    }

    private void spawnSingleBullet(float x, float y, float vy, boolean isWave) {
        for (Bullet b : bullets) {
            if (!b.active) {
                b.x = x;
                b.y = y;
                b.vy = vy;
                b.isWave = isWave;
                b.active = true;
                break;
            }
        }
    }

    private void spawnCoin(float x, float y) {
        for (Coin c : coins) {
            if (!c.active) {
                c.x = x;
                c.y = y;
                c.active = true;
                break;
            }
        }
    }

    private void spawnExplosion(float x, float y) {
        for (Particle p : particles) {
            p.x = x;
            p.y = y;
            p.vx = (float) (Math.random() * 2.2 - 1.1);
            p.vy = (float) (Math.random() * 2.2 - 1.1);
            p.life = 8 + (int) (Math.random() * 10);
            p.color = Math.random() > 0.5 ? 0xFFEF4444 : 0xFFF59E0B;
        }
    }

    @Override
    public void onRender(ProtoCanvas canvas) {
        canvas.clear(0xFF0A0A12); // Deep space background

        // 1. Starfield Background
        for (Star star : stars) {
            canvas.setPixel((int) star.x, (int) star.y, star.brightness);
        }

        // 2. Top HUD Bar (Demonstrating drawLine, drawImage, drawPixelMap, and drawSmallText)
        canvas.drawLine(0, 6, 127, 6, 0xFF1E293B);
        for (int i = 0; i < playerLives; i++) {
            canvas.drawImage("heart.png", 2 + (i * 6), 1);
        }
        if (weaponMode == 1) {
            canvas.drawPixelMap(SHIELD_ICON_PIXELS, 5, 5, 22, 1, false, false);
        }
        canvas.drawSmallText(weaponMode == 0 ? "TWIN" : "WAVE", 29, 1, 0xFF38BDF8);
        canvas.drawSmallText("SC:" + score, 52, 1, 0xFF10B981);
        canvas.drawSmallText("HI:" + highScore, 90, 1, 0xFFF59E0B);

        // 3. State Overlays (PAUSED / GAME OVER)
        if (currentState == State.PAUSED) {
            canvas.drawRect(24, 9, 80, 21, 0xFFF59E0B);
            canvas.drawText("PAUSED", 46, 18, 0xFFF59E0B);
            canvas.drawSmallText("PRESS START", 42, 23, 0xFF9CA3AF);
            return;
        }
        if (currentState == State.GAME_OVER) {
            canvas.drawRect(18, 8, 92, 22, 0xFFEF4444);
            canvas.drawText("GAME OVER", 37, 17, 0xFFEF4444);
            canvas.drawSmallText("START:RETRY  HI:" + highScore, 24, 22, 0xFFE5E7EB);
            return;
        }

        // 4. EMP Shockwave Ring (Demonstrating drawCircle)
        if (empShockwaveRadius >= 0) {
            canvas.drawCircle(empOriginX, empOriginY, empShockwaveRadius, 0xFF38BDF8);
        }

        // 5. Explosion Particles
        for (Particle p : particles) {
            if (p.life > 0) {
                canvas.setPixel((int) p.x, (int) p.y, p.color);
            }
        }

        // 6. Collectible Coins (Demonstrating animated sprite sheets via drawSpriteFrame)
        int coinFrame = (int) ((getFrameCount() / 4) % 4);
        for (Coin c : coins) {
            if (c.active) {
                canvas.drawSpriteFrame(
                        "coin_sheet.png", 5, 5, coinFrame, (int) c.x, (int) c.y, false, false
                );
            }
        }

        // 7. Player Starfighter (Animated 8x6 sprite sheet)
        int shipFrame = (int) ((getFrameCount() / 3) % 3);
        canvas.drawSpriteFrame(
                "player_sheet.png", 8, 6, shipFrame, (int) playerX, (int) playerY, false, false
        );

        // 8. Weapon Projectiles (Demonstrating drawLine & fillCircle)
        for (Bullet b : bullets) {
            if (b.active) {
                int bx = (int) b.x;
                int by = (int) b.y;
                if (b.isWave) {
                    canvas.fillCircle(bx, by, 1, 0xFF38BDF8);
                } else {
                    canvas.drawLine(bx, by, bx + 2, by, 0xFF34D399);
                }
            }
        }

        // 9. Enemies (Animated 6x6 alien fighters + circular asteroids)
        int enemyFrame = (int) ((getFrameCount() / 4) % 4);
        for (Enemy e : enemies) {
            if (e.active) {
                int ex = (int) e.x;
                int ey = (int) e.y;
                if (e.type == 0) {
                    canvas.fillCircle(ex + 2, ey + 2, 2, 0xFF6B7280);
                    canvas.drawCircle(ex + 2, ey + 2, 2, 0xFF9CA3AF);
                } else {
                    canvas.drawSpriteFrame(
                            "enemy_sheet.png", 6, 6, enemyFrame, ex, ey, true, false
                    );
                }
            }
        }
    }
}