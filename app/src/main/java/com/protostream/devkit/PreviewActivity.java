package com.protostream.devkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import yen.proto.stream.sdk.GameInfo;
import yen.proto.stream.sdk.Input;
import yen.proto.stream.sdk.ProtoCanvas;
import yen.proto.stream.sdk.ProtoGame;

/**
 * Built-in local emulator activity allowing developers to press "Run" in Android Studio
 * to test and step-debug their custom ProtoGame on a PC emulator or Android device
 * using on-screen touch controls, a PC keyboard, an Xbox / Bluetooth gamepad, or
 * the built-in visor tilt simulator.
 */
public class PreviewActivity extends AppCompatActivity implements SensorEventListener {

    private static final int MATRIX_WIDTH = 128;
    private static final int MATRIX_HEIGHT = 32;

    private PreviewCanvas previewCanvas;
    private PreviewInput previewInput;
    private ProtoGame game;
    private volatile boolean isRunning = false;
    private final Handler frameHandler = new Handler(Looper.getMainLooper());
    private SensorManager sensorManager;
    private volatile float sensorTiltX = 0f;
    private volatile float sensorTiltY = 0f;
    private volatile float manualTiltX = 0f;
    private volatile float manualTiltY = 0f;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewCanvas = new PreviewCanvas();
        previewInput = new PreviewInput();
        loadAssetsIntoCanvas(getAssets());

        game = new SampleGame();
        GameInfo info = game.getClass().getAnnotation(GameInfo.class);
        String gameTitle = (info != null && !info.title().isEmpty()) ? info.title() : "Custom Game";
        String gameAuthor = (info != null && !info.author().isEmpty()) ? info.author() : "DevKit";

        final SharedPreferences prefs = getSharedPreferences(
                "DevKitPreviewSave_" + gameTitle.replaceAll("[^a-zA-Z0-9_-]", "_"),
                Context.MODE_PRIVATE
        );
        game.attachStorageBridge(new ProtoGame.StorageBridge() {
            @Override
            public void saveInt(@NonNull String key, int value) {
                prefs.edit().putInt(key, value).apply();
            }

            @Override
            public int loadInt(@NonNull String key, int defaultValue) {
                return prefs.getInt(key, defaultValue);
            }

            @Override
            public void saveString(@NonNull String key, @NonNull String value) {
                prefs.edit().putString(key, value).apply();
            }

            @NonNull
            @Override
            public String loadString(@NonNull String key, @NonNull String defaultValue) {
                String val = prefs.getString(key, null);
                return val != null ? val : defaultValue;
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accel != null) {
                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
            }
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        TextView header = new TextView(this);
        header.setText(gameTitle + " — by " + gameAuthor + " (DevKit Live Preview)");
        header.setTextColor(0xFF10B981);
        header.setTextSize(14f);
        header.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setPadding(dp(12), dp(14), dp(12), dp(6));
        root.addView(header);

        final MatrixPreviewView displayView = new MatrixPreviewView(this, previewCanvas);
        displayView.setFocusable(true);
        displayView.setFocusableInTouchMode(true);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(
                screenWidth,
                screenWidth / 4
        );
        displayParams.topMargin = dp(8);
        displayParams.bottomMargin = dp(10);
        displayView.setLayoutParams(displayParams);
        root.addView(displayView);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setGravity(Gravity.CENTER);
        controls.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        controls.addView(createSingleDpadRow(Input.Button.UP, "UP"));
        controls.addView(createFourButtonRow());
        controls.addView(createSingleDpadRow(Input.Button.DOWN, "DOWN"));
        controls.addView(createSystemRow());
        controls.addView(createTiltSimulatorPanel());

        root.addView(controls);
        setContentView(root);
        displayView.requestFocus();

        isRunning = true;
        game.onInit();
        frameHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                long start = System.currentTimeMillis();
                float effectiveTiltX = Math.abs(manualTiltX) > 0.01f ? manualTiltX : sensorTiltX;
                float effectiveTiltY = Math.abs(manualTiltY) > 0.01f ? manualTiltY : sensorTiltY;
                previewInput.advanceFrame(effectiveTiltX, effectiveTiltY);
                game.incrementFrameCount();
                game.onUpdate(previewInput);
                game.onRender(previewCanvas);
                displayView.invalidate();
                long elapsed = System.currentTimeMillis() - start;
                long delay = Math.max(1L, 33L - elapsed);
                frameHandler.postDelayed(this, delay);
            }
        });
    }

    private void loadAssetsIntoCanvas(AssetManager am) {
        try {
            String[] list = am.list("");
            if (list == null) return;
            for (String item : list) {
                String lower = item.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".png") || lower.endsWith(".gif")
                        || lower.endsWith(".bmp") || lower.endsWith(".webp")
                        || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                    try (InputStream is = am.open(item)) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        opts.inScaled = false;
                        Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                        if (bmp != null) {
                            previewCanvas.registerAsset(item, bmp);
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private Button buildPadButton(String label, final Input.Button button, int wDp, int hDp) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setFocusable(false);
        b.setFocusableInTouchMode(false);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF333333);
        bg.setCornerRadius(dp(hDp / 3));
        bg.setStroke(dp(1), Color.DKGRAY);
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(wDp), dp(hDp));
        lp.setMargins(dp(6), dp(3), dp(6), dp(3));
        b.setLayoutParams(lp);
        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.5f);
                previewInput.setPressed(button, true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1.0f);
                previewInput.setPressed(button, false);
                v.performClick();
                return true;
            }
            return false;
        });
        return b;
    }

    private LinearLayout createSingleDpadRow(Input.Button button, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(70), dp(58)));
        row.addView(spacer);
        row.addView(buildPadButton(label, button, 70, 58));
        return row;
    }

    private LinearLayout createFourButtonRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(buildPadButton("LEFT", Input.Button.LEFT, 70, 58));
        row.addView(buildPadButton("RIGHT", Input.Button.RIGHT, 70, 58));
        View gap = new View(this);
        gap.setLayoutParams(new LinearLayout.LayoutParams(dp(16), dp(58)));
        row.addView(gap);
        row.addView(buildPadButton("A", Input.Button.A, 70, 58));
        row.addView(buildPadButton("B", Input.Button.B, 70, 58));
        return row;
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createSystemRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(10), 0, dp(4));
        row.addView(buildPadButton("SELECT", Input.Button.SELECT, 92, 40));
        row.addView(buildPadButton("START", Input.Button.START, 92, 40));

        Button boopBtn = new Button(this);
        boopBtn.setText("BOOP");
        boopBtn.setAllCaps(false);
        boopBtn.setFocusable(false);
        boopBtn.setFocusableInTouchMode(false);
        boopBtn.setTextColor(0xFF38BDF8);
        boopBtn.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1E293B);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), 0xFF38BDF8);
        boopBtn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(84), dp(40));
        lp.setMargins(dp(6), dp(3), dp(6), dp(3));
        boopBtn.setLayoutParams(lp);
        boopBtn.setOnClickListener(v -> previewInput.triggerBoopPulse());
        row.addView(boopBtn);
        return row;
    }

    /**
     * Builds an on-screen Tilt X / Tilt Y simulator bar so developers can test accelerometer
     * tilt directly inside the Running Devices panel without opening Extended Controls.
     */
    @SuppressLint("SetTextI18n")
    private LinearLayout createTiltSimulatorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setPadding(dp(20), dp(8), dp(20), dp(4));
        panel.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        final TextView tiltLabel = new TextView(this);
        tiltLabel.setText("Visor Tilt Simulator — X: 0.00  Y: 0.00");
        tiltLabel.setTextColor(0xFF9CA3AF);
        tiltLabel.setTextSize(11f);
        tiltLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tiltLabel.setGravity(Gravity.CENTER);
        panel.addView(tiltLabel);

        final SeekBar seekX = new SeekBar(this);
        seekX.setMax(200);
        seekX.setProgress(100);
        seekX.setFocusable(false);

        final SeekBar seekY = new SeekBar(this);
        seekY.setMax(200);
        seekY.setProgress(100);
        seekY.setFocusable(false);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                manualTiltX = (seekX.getProgress() - 100) / 100f;
                manualTiltY = (seekY.getProgress() - 100) / 100f;
                tiltLabel.setText(String.format(
                        Locale.US,
                        "Visor Tilt Simulator — X: %.2f  Y: %.2f",
                        manualTiltX,
                        manualTiltY
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        seekX.setOnSeekBarChangeListener(listener);
        seekY.setOnSeekBarChangeListener(listener);

        LinearLayout rowX = new LinearLayout(this);
        rowX.setOrientation(LinearLayout.HORIZONTAL);
        rowX.setGravity(Gravity.CENTER_VERTICAL);
        TextView lblX = new TextView(this);
        lblX.setText("Tilt X:");
        lblX.setTextColor(Color.LTGRAY);
        lblX.setTextSize(11f);
        rowX.addView(lblX);
        LinearLayout.LayoutParams sxLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        seekX.setLayoutParams(sxLp);
        rowX.addView(seekX);
        panel.addView(rowX);

        LinearLayout rowY = new LinearLayout(this);
        rowY.setOrientation(LinearLayout.HORIZONTAL);
        rowY.setGravity(Gravity.CENTER_VERTICAL);
        TextView lblY = new TextView(this);
        lblY.setText("Tilt Y:");
        lblY.setTextColor(Color.LTGRAY);
        lblY.setTextSize(11f);
        rowY.addView(lblY);
        LinearLayout.LayoutParams syLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        seekY.setLayoutParams(syLp);
        rowY.addView(seekY);

        Button resetBtn = new Button(this);
        resetBtn.setText("Center");
        resetBtn.setAllCaps(false);
        resetBtn.setTextSize(10f);
        resetBtn.setFocusable(false);
        resetBtn.setFocusableInTouchMode(false);
        resetBtn.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF374151);
        bg.setCornerRadius(dp(12));
        resetBtn.setBackground(bg);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(dp(64), dp(30));
        rLp.leftMargin = dp(6);
        resetBtn.setLayoutParams(rLp);
        resetBtn.setOnClickListener(v -> {
            seekX.setProgress(100);
            seekY.setProgress(100);
        });
        rowY.addView(resetBtn);
        panel.addView(rowY);

        return panel;
    }

    /**
     * Handles physical keyboard keys and Xbox / Bluetooth controller button events.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();

        // Allow 'B' key on keyboard or L3/R3 thumbstick click on Xbox controller to trigger Boop
        if (keyCode == KeyEvent.KEYCODE_B
                || keyCode == KeyEvent.KEYCODE_BUTTON_THUMBL
                || keyCode == KeyEvent.KEYCODE_BUTTON_THUMBR) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                previewInput.triggerBoopPulse();
            }
            return true;
        }

        // Many Xbox Wireless Controllers emit KEYCODE_BACK for the View/Select button
        boolean isGamepadSource = (event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0
                || (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0;
        if (keyCode == KeyEvent.KEYCODE_BACK && isGamepadSource) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                previewInput.setPressed(Input.Button.SELECT, true);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                previewInput.setPressed(Input.Button.SELECT, false);
            }
            return true;
        }

        Input.Button mapped = mapKeyCodeToButton(keyCode);
        if (mapped != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                previewInput.setPressed(mapped, true);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                previewInput.setPressed(mapped, false);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Maps keyboard keycodes and Xbox/gamepad button keycodes to SDK Input.Button constants.
     */
    private Input.Button mapKeyCodeToButton(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                return Input.Button.UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                return Input.Button.DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                return Input.Button.LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                return Input.Button.RIGHT;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_BUTTON_R2:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_Z:
                return Input.Button.A;
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_BUTTON_L2:
            case KeyEvent.KEYCODE_X:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return Input.Button.B;
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_P:
            case KeyEvent.KEYCODE_ESCAPE:
                return Input.Button.START;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_BUTTON_MODE:
            case KeyEvent.KEYCODE_TAB:
                return Input.Button.SELECT;
            default:
                return null;
        }
    }

    /**
     * Handles Xbox / Bluetooth controller analog sticks, D-Pad hat axes (AXIS_HAT_X / AXIS_HAT_Y),
     * and Left/Right analog triggers (LT / RT).
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean isJoystick = (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0
                || (event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0;
        if (isJoystick && event.getAction() == MotionEvent.ACTION_MOVE) {
            float axisX = event.getAxisValue(MotionEvent.AXIS_X);
            float axisY = event.getAxisValue(MotionEvent.AXIS_Y);
            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            previewInput.setPressed(Input.Button.LEFT, axisX < -0.5f || hatX < -0.5f);
            previewInput.setPressed(Input.Button.RIGHT, axisX > 0.5f || hatX > 0.5f);
            previewInput.setPressed(Input.Button.UP, axisY < -0.5f || hatY < -0.5f);
            previewInput.setPressed(Input.Button.DOWN, axisY > 0.5f || hatY > 0.5f);

            float rTrig = Math.max(
                    event.getAxisValue(MotionEvent.AXIS_RTRIGGER),
                    event.getAxisValue(MotionEvent.AXIS_GAS)
            );
            float lTrig = Math.max(
                    event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
                    event.getAxisValue(MotionEvent.AXIS_BRAKE)
            );
            previewInput.setPressed(Input.Button.A, rTrig > 0.5f);
            previewInput.setPressed(Input.Button.B, lTrig > 0.5f);
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorTiltX = Math.max(-1f, Math.min(1f, -event.values[0] / 9.81f));
            sensorTiltY = Math.max(-1f, Math.min(1f, event.values[1] / 9.81f));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        frameHandler.removeCallbacksAndMessages(null);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private static class MatrixPreviewView extends View {
        private final PreviewCanvas hostCanvas;
        private final Paint paint = new Paint();
        private final Rect src = new Rect(0, 0, MATRIX_WIDTH, MATRIX_HEIGHT);
        private final Rect dst = new Rect();

        MatrixPreviewView(Context context, PreviewCanvas canvas) {
            super(context);
            this.hostCanvas = canvas;
            paint.setFilterBitmap(false);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            dst.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(hostCanvas.bitmap, src, dst, paint);
        }
    }

    private static class PreviewInput implements Input {
        private final Map<Button, Boolean> live = new EnumMap<>(Button.class);
        private final Map<Button, Boolean> curr = new EnumMap<>(Button.class);
        private final Map<Button, Boolean> prev = new EnumMap<>(Button.class);
        private volatile boolean pendingBoop = false;
        private boolean currBoop = false;
        private boolean prevBoop = false;
        private float tiltX = 0f;
        private float tiltY = 0f;

        synchronized void setPressed(Button b, boolean pressed) {
            live.put(b, pressed);
        }

        void triggerBoopPulse() {
            pendingBoop = true;
        }

        synchronized void advanceFrame(float tx, float ty) {
            for (Button b : Button.getEntries()) {
                prev.put(b, Boolean.TRUE.equals(curr.get(b)));
                curr.put(b, Boolean.TRUE.equals(live.get(b)));
            }
            prevBoop = currBoop;
            currBoop = pendingBoop;
            pendingBoop = false;
            tiltX = tx;
            tiltY = ty;
        }

        @Override
        public synchronized boolean isPressed(@NonNull Button button) {
            return Boolean.TRUE.equals(curr.get(button));
        }

        @Override
        public synchronized boolean isJustPressed(@NonNull Button button) {
            return Boolean.TRUE.equals(curr.get(button)) && !Boolean.TRUE.equals(prev.get(button));
        }

        @Override
        public synchronized boolean isJustReleased(@NonNull Button button) {
            return !Boolean.TRUE.equals(curr.get(button)) && Boolean.TRUE.equals(prev.get(button));
        }

        @Override
        public boolean isBooped() {
            return currBoop;
        }

        @Override
        public boolean isJustBooped() {
            return currBoop && !prevBoop;
        }

        @Override
        public float getTiltX() {
            return tiltX;
        }

        @Override
        public float getTiltY() {
            return tiltY;
        }

        @Override
        public float getMicLevel() {
            return 0f;
        }
    }

    private static class PreviewCanvas implements ProtoCanvas {
        final Bitmap bitmap = Bitmap.createBitmap(
                MATRIX_WIDTH, MATRIX_HEIGHT, Bitmap.Config.ARGB_8888
        );
        private final Canvas canvas = new Canvas(bitmap);
        private final Paint paint = new Paint();
        private final Paint textPaint = new Paint();
        private final Map<String, Bitmap> assets = new HashMap<>();

        PreviewCanvas() {
            paint.setAntiAlias(false);
            paint.setDither(false);
            textPaint.setAntiAlias(false);
            textPaint.setDither(false);
            textPaint.setTextSize(10f);
            textPaint.setTypeface(Typeface.MONOSPACE);
            textPaint.setFakeBoldText(true);
        }

        void registerAsset(String path, Bitmap bmp) {
            String key = path.toLowerCase(Locale.ROOT);
            assets.put(key, bmp);
            int slash = key.lastIndexOf('/');
            if (slash >= 0) {
                assets.put(key.substring(slash + 1), bmp);
            }
        }

        private Bitmap findAsset(String name) {
            String key = name.trim().toLowerCase(Locale.ROOT);
            if (key.startsWith("/")) key = key.substring(1);
            if (key.startsWith("assets/")) key = key.substring(7);
            Bitmap bmp = assets.get(key);
            if (bmp != null) return bmp;
            int slash = key.lastIndexOf('/');
            return slash >= 0 ? assets.get(key.substring(slash + 1)) : null;
        }

        @Override
        public void clear(int color) {
            canvas.drawColor(color);
        }

        @Override
        public void drawText(@NonNull String text, int x, int y, int color) {
            textPaint.setColor(color);
            canvas.drawText(text, x, y, textPaint);
        }

        @Override
        public void drawSmallText(@NonNull String text, int x, int y, int color) {
            int curX = x;
            int curY = y;
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\n') {
                    curX = x;
                    curY += 6;
                    continue;
                }
                int mask = getSmallGlyphMask(ch);
                if (mask != 0) {
                    for (int row = 0; row < 5; row++) {
                        for (int col = 0; col < 3; col++) {
                            int bit = (4 - row) * 3 + (2 - col);
                            if (((mask >> bit) & 1) != 0) {
                                setPixel(curX + col, curY + row, color);
                            }
                        }
                    }
                }
                curX += 4;
            }
        }

        @Override
        public void fillRect(int x, int y, int width, int height, int color) {
            paint.setColor(color);
            canvas.drawRect(x, y, x + width, y + height, paint);
        }

        @Override
        public void drawRect(int x, int y, int width, int height, int color) {
            if (width <= 0 || height <= 0) return;
            int right = x + width - 1;
            int bottom = y + height - 1;
            for (int px = x; px <= right; px++) {
                setPixel(px, y, color);
                setPixel(px, bottom, color);
            }
            for (int py = y; py <= bottom; py++) {
                setPixel(x, py, color);
                setPixel(right, py, color);
            }
        }

        @Override
        public void drawLine(int x1, int y1, int x2, int y2, int color) {
            int cx = x1, cy = y1;
            int dx = Math.abs(x2 - x1), dy = -Math.abs(y2 - y1);
            int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
            int err = dx + dy;
            while (true) {
                setPixel(cx, cy, color);
                if (cx == x2 && cy == y2) break;
                int e2 = 2 * err;
                if (e2 >= dy) { err += dy; cx += sx; }
                if (e2 <= dx) { err += dx; cy += sy; }
            }
        }

        @Override
        public void drawCircle(int cx, int cy, int radius, int color) {
            if (radius < 0) return;
            if (radius == 0) { setPixel(cx, cy, color); return; }
            int x = radius, y = 0, err = 1 - x;
            while (x >= y) {
                setPixel(cx + x, cy + y, color);
                setPixel(cx + y, cy + x, color);
                setPixel(cx - y, cy + x, color);
                setPixel(cx - x, cy + y, color);
                setPixel(cx - x, cy - y, color);
                setPixel(cx - y, cy - x, color);
                setPixel(cx + y, cy - x, color);
                setPixel(cx + x, cy - y, color);
                y++;
                if (err < 0) err += 2 * y + 1;
                else { x--; err += 2 * (y - x + 1); }
            }
        }

        @Override
        public void fillCircle(int cx, int cy, int radius, int color) {
            if (radius < 0) return;
            if (radius == 0) { setPixel(cx, cy, color); return; }
            int x = radius, y = 0, err = 1 - x;
            while (x >= y) {
                for (int px = cx - x; px <= cx + x; px++) {
                    setPixel(px, cy + y, color);
                    setPixel(px, cy - y, color);
                }
                for (int px = cx - y; px <= cx + y; px++) {
                    setPixel(px, cy + x, color);
                    setPixel(px, cy - x, color);
                }
                y++;
                if (err < 0) err += 2 * y + 1;
                else { x--; err += 2 * (y - x + 1); }
            }
        }

        @Override
        public void setPixel(int x, int y, int color) {
            if (x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                int alpha = color >>> 24;
                if (alpha == 0) return;
                if (alpha == 255) {
                    bitmap.setPixel(x, y, color);
                } else {
                    int dst = bitmap.getPixel(x, y);
                    int invA = 255 - alpha;
                    int r = (((color >> 16) & 0xFF) * alpha + ((dst >> 16) & 0xFF) * invA) / 255;
                    int g = (((color >> 8) & 0xFF) * alpha + ((dst >> 8) & 0xFF) * invA) / 255;
                    int b = ((color & 0xFF) * alpha + (dst & 0xFF) * invA) / 255;
                    bitmap.setPixel(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }

        @Override
        public void drawImage(@NonNull String assetName, int x, int y) {
            drawImageFlipped(assetName, x, y, false, false);
        }

        @Override
        public void drawImageFlipped(
                @NonNull String assetName, int x, int y, boolean flipX, boolean flipY
        ) {
            Bitmap bmp = findAsset(assetName);
            if (bmp == null) return;
            blitRegion(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), x, y, flipX, flipY);
        }

        @Override
        public void drawSpriteFrame(
                @NonNull String assetName,
                int frameWidth,
                int frameHeight,
                int frameIndex,
                int x,
                int y,
                boolean flipX,
                boolean flipY
        ) {
            if (frameWidth <= 0 || frameHeight <= 0 || frameIndex < 0) return;
            Bitmap bmp = findAsset(assetName);
            if (bmp == null) return;
            int cols = Math.max(1, bmp.getWidth() / frameWidth);
            int rows = Math.max(1, bmp.getHeight() / frameHeight);
            int total = Math.max(1, cols * rows);
            int idx = frameIndex % total;
            int srcX = (idx % cols) * frameWidth;
            int srcY = (idx / cols) * frameHeight;
            blitRegion(bmp, srcX, srcY, frameWidth, frameHeight, x, y, flipX, flipY);
        }

        private void blitRegion(
                Bitmap bmp,
                int srcX,
                int srcY,
                int w,
                int h,
                int dstX,
                int dstY,
                boolean flipX,
                boolean flipY
        ) {
            int maxW = Math.min(w, bmp.getWidth() - srcX);
            int maxH = Math.min(h, bmp.getHeight() - srcY);
            if (maxW <= 0 || maxH <= 0) return;
            for (int row = 0; row < maxH; row++) {
                int sy = srcY + (flipY ? (maxH - 1 - row) : row);
                int py = dstY + row;
                if (py < 0 || py >= MATRIX_HEIGHT) continue;
                for (int col = 0; col < maxW; col++) {
                    int sx = srcX + (flipX ? (maxW - 1 - col) : col);
                    int px = dstX + col;
                    if (px < 0 || px >= MATRIX_WIDTH) continue;
                    setPixel(px, py, bmp.getPixel(sx, sy));
                }
            }
        }

        @Override
        public void drawPixelMap(
                @NonNull int[] pixels,
                int width,
                int height,
                int x,
                int y,
                boolean flipX,
                boolean flipY
        ) {
            if (width <= 0 || height <= 0 || pixels.length < width * height) {
                return;
            }
            for (int row = 0; row < height; row++) {
                int srcRow = flipY ? (height - 1 - row) : row;
                int py = y + row;
                if (py < 0 || py >= MATRIX_HEIGHT) continue;
                for (int col = 0; col < width; col++) {
                    int srcCol = flipX ? (width - 1 - col) : col;
                    int px = x + col;
                    if (px < 0 || px >= MATRIX_WIDTH) continue;
                    setPixel(px, py, pixels[srcRow * width + srcCol]);
                }
            }
        }

        private static int getSmallGlyphMask(char ch) {
            switch (Character.toUpperCase(ch)) {
                case 'A': return 0b010_101_111_101_101;
                case 'B': return 0b110_101_110_101_110;
                case 'C': return 0b011_100_100_100_011;
                case 'D': return 0b110_101_101_101_110;
                case 'E': return 0b111_100_110_100_111;
                case 'F': return 0b111_100_110_100_100;
                case 'G': return 0b011_100_101_101_011;
                case 'H': return 0b101_101_111_101_101;
                case 'I': return 0b111_010_010_010_111;
                case 'J': return 0b001_001_001_101_010;
                case 'K': return 0b101_101_110_101_101;
                case 'L': return 0b100_100_100_100_111;
                case 'M': return 0b101_111_111_101_101;
                case 'N': return 0b111_101_101_101_101;
                case 'O':
                case '0': return 0b111_101_101_101_111;
                case 'P': return 0b110_101_110_100_100;
                case 'Q': return 0b111_101_101_111_001;
                case 'R': return 0b110_101_110_101_101;
                case 'S':
                case '5': return 0b011_100_010_001_110;
                case 'T': return 0b111_010_010_010_010;
                case 'U': return 0b101_101_101_101_111;
                case 'V': return 0b101_101_101_101_010;
                case 'W': return 0b101_101_111_111_101;
                case 'X': return 0b101_101_010_101_101;
                case 'Y': return 0b101_101_010_010_010;
                case 'Z': return 0b111_001_010_100_111;
                case '1': return 0b010_110_010_010_111;
                case '2': return 0b110_001_010_100_111;
                case '3': return 0b110_001_010_001_110;
                case '4': return 0b101_101_111_001_001;
                case '6': return 0b011_100_111_101_111;
                case '7': return 0b111_001_010_010_010;
                case '8': return 0b111_101_111_101_111;
                case '9': return 0b111_101_111_001_110;
                case ':': return 0b000_010_000_010_000;
                case '.': return 0b000_000_000_000_010;
                case ',': return 0b000_000_000_010_100;
                case '!': return 0b010_010_010_000_010;
                case '?': return 0b110_001_010_000_010;
                case '-': return 0b000_000_111_000_000;
                case '+': return 0b000_010_111_010_000;
                case '/': return 0b001_001_010_100_100;
                case '=': return 0b000_111_000_111_000;
                case '%': return 0b101_001_010_100_101;
                case '(': return 0b001_010_010_010_001;
                case ')': return 0b100_010_010_010_100;
                default: return 0;
            }
        }
    }
}