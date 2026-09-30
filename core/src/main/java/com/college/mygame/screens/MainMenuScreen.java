package com.college.mygame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.college.mygame.MainGame;
import com.college.mygame.utils.AudioManager;

public class MainMenuScreen extends ScreenAdapter {
    private final MainGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;

    // Background & Poster Overlay
    private Texture background;
    private Texture posterTrio;
    private float stateTime = 0f;

    // UI Buttons
    private Texture btnStart;
    private Texture btnScores;
    private Texture btnSettings;

    private BitmapFont font;

    private int selectedIndex = 0; // 0 = Start, 1 = Scores, 2 = Settings
    private final int TOTAL_BUTTONS = 3;

    // Button bounds [x, y, width, height]
    private float[] startBounds = new float[4];
    private float[] scoreBounds = new float[4];
    private float[] settingsBounds = new float[4];

    // Poster bounds [x, y, width, height] - Shifted up and to the right
    private float posterBaseX = 120f;
    private float posterBaseY = 300f;
    private float posterWidth;
    private float posterHeight;

    public MainMenuScreen(MainGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();

        // Load Textures
        background = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu.png"));
        posterTrio = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu_poster.png"));

        btnStart = new Texture(Gdx.files.internal("ui/buttons/btn_start.png"));
        btnScores = new Texture(Gdx.files.internal("ui/buttons/btn_scores.png"));
        btnSettings = new Texture(Gdx.files.internal("ui/buttons/btn_settings.png"));

        // Scale poster proportionally into the upper-left sky area
        float posterScale = 0.25f;
        posterWidth = posterTrio.getWidth() * posterScale;
        posterHeight = posterTrio.getHeight() * posterScale;

        // Button uniform scale
        float buttonScale = 0.35f;

        // 1. START BUTTON: Right archway
        float startW = btnStart.getWidth() * buttonScale;
        float startH = btnStart.getHeight() * buttonScale;
        startBounds[0] = 640;
        startBounds[1] = 380;
        startBounds[2] = startW;
        startBounds[3] = startH;

        // 2. SCORES BUTTON: Lower-left ledge
        float scoreW = btnScores.getWidth() * buttonScale;
        float scoreH = btnScores.getHeight() * buttonScale;
        scoreBounds[0] = 130;
        scoreBounds[1] = 180;
        scoreBounds[2] = scoreW;
        scoreBounds[3] = scoreH;

        // 3. SETTINGS BUTTON: Lower down
        float settingsW = btnSettings.getWidth() * buttonScale;
        float settingsH = btnSettings.getHeight() * buttonScale;
        settingsBounds[0] = 180;
        settingsBounds[1] = 90;
        settingsBounds[2] = settingsW;
        settingsBounds[3] = settingsH;

        font = new BitmapFont();
        font.getData().setScale(1.1f);

        // Play main theme music across menu screens
        AudioManager.playMainTheme();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // 1. Draw Static Background
        game.batch.draw(background, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);

        // 2. Draw Poster with floating wobble effect
        float wobbleY = posterBaseY + (float) Math.sin(stateTime * 2.2f) * 4.0f;
        game.batch.draw(posterTrio, posterBaseX, wobbleY, posterWidth, posterHeight);

        // 3. Draw Buttons
        drawButton(btnStart, startBounds, 0);
        drawButton(btnScores, scoreBounds, 1);
        drawButton(btnSettings, settingsBounds, 2);

        // 4. Instructions
        font.setColor(Color.WHITE);
        font.draw(game.batch, "[MOUSE] Click to select   |   [W/S] or [UP/DOWN] to hover, [ENTER] to confirm",
            150, 35);

        game.batch.end();
    }

    private void drawButton(Texture tex, float[] bounds, int index) {
        if (selectedIndex == index) {
            game.batch.setColor(Color.GOLD);
        } else {
            game.batch.setColor(Color.WHITE);
        }
        game.batch.draw(tex, bounds[0], bounds[1], bounds[2], bounds[3]);
        game.batch.setColor(Color.WHITE);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = TOTAL_BUTTONS - 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex++;
            if (selectedIndex >= TOTAL_BUTTONS) selectedIndex = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            executeSelectedAction();
        }

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        if (isMouseOver(startBounds)) selectedIndex = 0;
        else if (isMouseOver(scoreBounds)) selectedIndex = 1;
        else if (isMouseOver(settingsBounds)) selectedIndex = 2;

        if (Gdx.input.justTouched()) {
            if (isMouseOver(startBounds) || isMouseOver(scoreBounds) || isMouseOver(settingsBounds)) {
                executeSelectedAction();
            }
        }
    }

    private boolean isMouseOver(float[] bounds) {
        return mousePos.x >= bounds[0] && mousePos.x <= bounds[0] + bounds[2] &&
            mousePos.y >= bounds[1] && mousePos.y <= bounds[1] + bounds[3];
    }

    private void executeSelectedAction() {
        AudioManager.playButtonClick();
        if (selectedIndex == 0) {
            game.setScreen(new ModeSelectScreen(game));
        } else if (selectedIndex == 1) {
            game.setScreen(new ScoresScreen(game));
        } else if (selectedIndex == 2) {
            game.setScreen(new SettingsScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        background.dispose();
        posterTrio.dispose();
        btnStart.dispose();
        btnScores.dispose();
        btnSettings.dispose();
        font.dispose();
    }
}
