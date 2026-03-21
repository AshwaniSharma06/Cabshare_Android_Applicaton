package com.example.flappybird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private int screenWidth, screenHeight;
    private Paint paint;
    private Random random;

    // Game Objects
    private float birdX, birdY, birdVelocity;
    private Rect birdRect;
    private List<Rect> pillars;
    private int pillarWidth = 200;
    private int pillarGap = 400;
    private int pillarSpeed = 10;
    private int score = 0;

    // Physics Constants (Easy Difficulty)
    private static final float GRAVITY = 0.6f; // Lower gravity for easier gameplay
    private static final float FLAP_STRENGTH = -15;

    public GameView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        paint = new Paint();
        random = new Random();
        pillars = new ArrayList<>();
        initGame();
    }

    private void initGame() {
        birdX = screenWidth / 4;
        birdY = screenHeight / 2;
        birdVelocity = 0;
        birdRect = new Rect();
        pillars.clear();
        score = 0;
        // Create initial pillars off-screen
        for (int i = 0; i < 4; i++) {
            int pillarX = screenWidth + i * (screenWidth / 2);
            createPillar(pillarX);
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        // Bird physics
        birdVelocity += GRAVITY;
        birdY += birdVelocity;
        birdRect.set((int)birdX - 25, (int)birdY - 25, (int)birdX + 25, (int)birdY + 25);

        // Move pillars
        for (int i = 0; i < pillars.size(); i += 2) {
            Rect topPillar = pillars.get(i);
            Rect bottomPillar = pillars.get(i + 1);

            topPillar.left -= pillarSpeed;
            topPillar.right -= pillarSpeed;
            bottomPillar.left -= pillarSpeed;
            bottomPillar.right -= pillarSpeed;

            // Check for scoring
            if (topPillar.right < birdX && !topPillar.isEmpty()) {
                 score++;
                 // Mark as passed to not score again
                 topPillar.setEmpty();
            }

            // Collision detection
            if (Rect.intersects(birdRect, topPillar) || Rect.intersects(birdRect, bottomPillar)) {
                isPlaying = false; // Game Over
            }
        }

        // Ground collision
        if (birdY > screenHeight - 150) { // 150 is grass height
            isPlaying = false; // Game Over
        }
        // Sky collision
        if(birdY < 0) {
            birdY = 0;
            birdVelocity = 0;
        }


        // Remove off-screen pillars and add new ones
        if (pillars.get(0).right < 0) {
            pillars.remove(0);
            pillars.remove(0);
            createPillar(pillars.get(pillars.size() - 2).left + (screenWidth / 2));
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // Background (Sky)
            canvas.drawColor(Color.parseColor("#70C5CE"));

            // Clouds
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenWidth / 5, screenHeight / 6, 100, paint);
            canvas.drawCircle(screenWidth / 2, screenHeight / 8, 150, paint);
            canvas.drawCircle(screenWidth * 4 / 5, screenHeight / 7, 120, paint);


            // Draw pillars
            paint.setColor(Color.parseColor("#008000")); // Green
            for (Rect pillar : pillars) {
                canvas.drawRect(pillar, paint);
            }

            // Ground / Grass
            paint.setColor(Color.parseColor("#A0522D")); // Dirt color
            canvas.drawRect(0, screenHeight - 150, screenWidth, screenHeight, paint);
            paint.setColor(Color.parseColor("#228B22")); // Grass color
            canvas.drawRect(0, screenHeight - 150, screenWidth, screenHeight - 130, paint);


            // Draw bird
            paint.setColor(Color.YELLOW);
            canvas.drawRect(birdRect, paint);

            // Draw Score
            paint.setColor(Color.WHITE);
            paint.setTextSize(100);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(score), screenWidth / 2, 150, paint);
            
            // Game Over
            if(!isPlaying && birdY > 50) { //Don't show on initial fall
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game Over", screenWidth / 2, screenHeight / 2, paint);
            }


            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(17); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isPlaying) {
                // Restart game
                initGame();
                resume();
            } else {
                // Flap
                birdVelocity = FLAP_STRENGTH;
            }
        }
        return true;
    }

    private void createPillar(int pillarX) {
        int pillarTopHeight = random.nextInt(screenHeight - pillarGap - 400) + 200;
        Rect topPillar = new Rect(pillarX, 0, pillarX + pillarWidth, pillarTopHeight);
        Rect bottomPillar = new Rect(pillarX, pillarTopHeight + pillarGap, pillarX + pillarWidth, screenHeight - 150);
        pillars.add(topPillar);
        pillars.add(bottomPillar);
    }
}
