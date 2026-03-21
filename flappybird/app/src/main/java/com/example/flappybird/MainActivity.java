package com.example.flappybird;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set our GameView as the content of the activity
        gameView = new GameView(this);
        setContentView(gameView);
    }
}
