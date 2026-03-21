package com.example.flappybird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {

    private final Paint paint;
    private float birdY;
    private float birdVelocity = 0;
    private final float flapStrength = -25; // Slightly reduced flap strength for balance
    private final float birdX = 150;
    private final float birdSize = 100;

    private final Handler handler;
    private final Runnable gameLoop;

    private final List<Pillar> pillars = new ArrayList<>();
    private long lastPillarTime = 0;
    private int score = 0;
    private boolean gameOver = false;

    private SoundPool soundPool;
    private final int flapSoundId;
    private final int pointSoundId;
    private final int hitSoundId;

    public GameView(Context context) {
        super(context);
        paint = new Paint();
        birdY = getHeight() / 2f;

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sounds - requires sound files in res/raw/
        flapSoundId = soundPool.load(context, R.raw.flap, 1);
        pointSoundId = soundPool.load(context, R.raw.point, 1);
        hitSoundId = soundPool.load(context, R.raw.hit, 1);


        handler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    update();
                }
                invalidate(); // Redraw the view
                handler.postDelayed(this, 16); // roughly 60 FPS
            }
        };
        handler.post(gameLoop);

        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (gameOver) {
                    resetGame();
                } else {
                    birdVelocity = flapStrength;
                    soundPool.play(flapSoundId, 1, 1, 0, 0, 1);
                }
            }
            return true;
        });
    }

    private void update() {
        // Reduced gravity to make the game easier
        float gravity = 0.8f;
        birdVelocity += gravity;
        birdY += birdVelocity;

        // Ground collision
        if (birdY + birdSize / 2 > getHeight() - 200) {
            if (!gameOver) { // Play sound only on the first hit
                soundPool.play(hitSoundId, 1, 1, 0, 0, 1);
            }
            gameOver = true;
        }

        // Pillar generation
        long now = System.currentTimeMillis();
        if (now - lastPillarTime > 2000) { // Add a pillar every 2 seconds
            lastPillarTime = now;
            pillars.add(new Pillar(getWidth(), getHeight()));
        }

        RectF birdRect = new RectF(birdX - birdSize / 2, birdY - birdSize / 2, birdX + birdSize / 2, birdY + birdSize / 2);

        // Update and check pillars
        List<Pillar> toRemove = new ArrayList<>();
        for (Pillar pillar : pillars) {
            pillar.update();
            if (pillar.x + Pillar.width < 0) {
                toRemove.add(pillar);
                if (!pillar.isPassed()) {
                   score++;
                   pillar.setPassed(true);
                   soundPool.play(pointSoundId, 1, 1, 0, 0, 1);
                }
            }

            RectF topPillar = new RectF(pillar.x, 0, pillar.x + Pillar.width, pillar.gapY - (Pillar.gapHeight / 2));
            RectF bottomPillar = new RectF(pillar.x, pillar.gapY + (Pillar.gapHeight / 2), pillar.x + Pillar.width, getHeight());

            if (RectF.intersects(birdRect, topPillar) || RectF.intersects(birdRect, bottomPillar)) {
                if (!gameOver) { // Play sound only on the first hit
                    soundPool.play(hitSoundId, 1, 1, 0, 0, 1);
                }
                gameOver = true;
            }
        }
        pillars.removeAll(toRemove);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw sky background
        canvas.drawColor(Color.CYAN);

        // Draw clouds
        paint.setColor(Color.WHITE);
        canvas.drawRect(200, 200, 400, 300, paint);
        canvas.drawRect(500, 150, 750, 280, paint);

        // Draw pillars
        for (Pillar pillar : pillars) {
            pillar.draw(canvas, paint);
        }

        // Draw land
        paint.setColor(Color.rgb(112, 224, 0)); // A grassy green
        canvas.drawRect(0, getHeight() - 200, getWidth(), getHeight(), paint);

        // Draw bird
        paint.setColor(Color.YELLOW);
        canvas.drawRect(birdX - birdSize / 2, birdY - birdSize / 2, birdX + birdSize / 2, birdY + birdSize / 2, paint);

        // Draw score and Game Over text
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        canvas.drawText("Score: " + score, 50, 150, paint);

        if (gameOver) {
            paint.setTextSize(150);
            canvas.drawText("Game Over", getWidth() / 2 - 400, getHeight() / 2, paint);
            paint.setTextSize(80);
            canvas.drawText("Tap to Restart", getWidth() / 2 - 350, getHeight() / 2 + 150, paint);
        }
    }

    private void resetGame() {
        birdY = getHeight() / 2f;
        birdVelocity = 0;
        pillars.clear();
        score = 0;
        gameOver = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Stop the game loop to prevent memory leaks
        handler.removeCallbacks(gameLoop);
        // Release the sound pool resources
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
