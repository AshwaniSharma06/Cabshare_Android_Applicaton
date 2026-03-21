package com.example.flappybird;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Pillar {

    public float x, gapY;
    public static final float width = 200;
    public static final float gapHeight = 400;

    private Random random = new Random();

    public Pillar(float startX, int screenHeight) {
        this.x = startX;
        // Randomize the gap position
        this.gapY = random.nextInt(screenHeight - 800) + 400; // Gap between 400 and screenHeight - 400
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.GREEN);

        // Draw upper pillar
        canvas.drawRect(x, 0, x + width, gapY - (gapHeight / 2), paint);

        // Draw lower pillar
        canvas.drawRect(x, gapY + (gapHeight / 2), x + width, canvas.getHeight(), paint);
    }

    public void update() {
        x -= 10; // Move pillar to the left
    }

    public boolean isPassed() {
        return false;
    }

    public void setPassed(boolean b) {
    }
}
