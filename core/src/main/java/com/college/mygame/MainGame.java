package com.college.mygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.college.mygame.screens.MainMenuScreen;
import com.college.mygame.utils.AudioManager;

public class MainGame extends Game {

    public SpriteBatch batch;
    public static final int VIRTUAL_WIDTH = 960;
    public static final int VIRTUAL_HEIGHT = 540;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Load all audio into memory at startup
        AudioManager.load();

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();

        // Clean up audio files to prevent memory leaks
        AudioManager.dispose();

        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
