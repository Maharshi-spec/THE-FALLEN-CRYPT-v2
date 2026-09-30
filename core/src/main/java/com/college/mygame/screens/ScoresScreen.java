package com.college.mygame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.college.mygame.MainGame;

public class ScoresScreen extends ScreenAdapter {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;
    private ShapeRenderer shapeRenderer;

    private Texture background;
    private Texture btnBack;
    private BitmapFont font;

    // UI State
    private boolean isPvPTabActive = true;
    // 0 = Back Button, 1 = PvP Tab, 2 = Dungeon Tab
    private int selectedIndex = 1;

    // Bounds (x, y, width, height)
    private final float[] backBounds = {20, MainGame.VIRTUAL_HEIGHT - 80, 60, 60};
    private final float[] pvpTabBounds = {180, MainGame.VIRTUAL_HEIGHT - 100, 300, 50};
    private final float[] dungeonTabBounds = {480, MainGame.VIRTUAL_HEIGHT - 100, 300, 50};

    // Colors
    private final Color colorSilver = new Color(0.75f, 0.75f, 0.75f, 1f);
    private final Color colorGray = new Color(0.4f, 0.4f, 0.4f, 1f);
    private final Color colorHighlight = new Color(0.9f, 0.8f, 0.2f, 1f);

    public ScoresScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();
        shapeRenderer = new ShapeRenderer();

        // Load Assets
        background = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu.png"));
        btnBack = new Texture(Gdx.files.internal("ui/buttons/btn_back.png"));
        font = new BitmapFont();
        font.getData().setScale(1.2f);
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        game.batch.begin();
        // Draw dimmed background (dark gray tint)
        game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        game.batch.draw(background, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        game.batch.setColor(Color.WHITE); // Reset for UI
        game.batch.end();

        // Draw solid shapes for Tabs
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // PvP Tab Color
        shapeRenderer.setColor(isPvPTabActive ? colorGray : colorSilver);
        shapeRenderer.rect(pvpTabBounds[0], pvpTabBounds[1], pvpTabBounds[2], pvpTabBounds[3]);

        // Dungeon Tab Color
        shapeRenderer.setColor(!isPvPTabActive ? colorGray : colorSilver);
        shapeRenderer.rect(dungeonTabBounds[0], dungeonTabBounds[1], dungeonTabBounds[2], dungeonTabBounds[3]);

        shapeRenderer.end();

        // Draw Text and Buttons
        game.batch.begin();

        // Back Button
        if (selectedIndex == 0) game.batch.setColor(Color.GOLD);
        game.batch.draw(btnBack, backBounds[0], backBounds[1], backBounds[2], backBounds[3]);
        game.batch.setColor(Color.WHITE);

        // Tab Text
        font.setColor(selectedIndex == 1 ? colorHighlight : Color.WHITE);
        font.draw(game.batch, "PvP SCORES", pvpTabBounds[0] + 80, pvpTabBounds[1] + 35);

        font.setColor(selectedIndex == 2 ? colorHighlight : Color.WHITE);
        font.draw(game.batch, "DUNGEON SCORES", dungeonTabBounds[0] + 60, dungeonTabBounds[1] + 35);

        font.setColor(Color.WHITE);

        // Draw Data Based on Active Tab
        if (isPvPTabActive) {
            drawPvPData();
        } else {
            drawDungeonData();
        }

        // Hints
        font.draw(game.batch, "[MOUSE] Click   |   [LEFT/RIGHT] Change Tab   |   [ESC/BACKSPACE] Go Back", 180, 40);
        game.batch.end();
    }

    private void drawPvPData() {
        float startY = MainGame.VIRTUAL_HEIGHT - 180;
        font.setColor(Color.GOLD);
        font.draw(game.batch, "MATCH", 200, startY);
        font.draw(game.batch, "P1", 380, startY);
        font.draw(game.batch, "P2", 530, startY);
        font.draw(game.batch, "VICTOR", 680, startY);
        font.setColor(Color.WHITE);

        font.draw(game.batch, "001", 210, startY - 50);
        font.draw(game.batch, "Knight", 370, startY - 50);
        font.draw(game.batch, "Mage", 520, startY - 50);
        font.draw(game.batch, "Player 1", 680, startY - 50);
    }

    private void drawDungeonData() {
        float startY = MainGame.VIRTUAL_HEIGHT - 180;
        font.setColor(Color.GOLD);
        font.draw(game.batch, "RUN", 100, startY);
        font.draw(game.batch, "SIZE", 200, startY);
        font.draw(game.batch, "P1", 300, startY);
        font.draw(game.batch, "P2", 420, startY);
        font.draw(game.batch, "P3", 540, startY);
        font.draw(game.batch, "LEVEL", 660, startY);
        font.draw(game.batch, "STATUS", 780, startY);
        font.setColor(Color.WHITE);

        font.draw(game.batch, "001", 105, startY - 50);
        font.draw(game.batch, "3", 215, startY - 50);
        font.draw(game.batch, "Knight", 295, startY - 50);
        font.draw(game.batch, "Mage", 415, startY - 50);
        font.draw(game.batch, "Archer", 535, startY - 50);
        font.draw(game.batch, "Floor 5", 660, startY - 50);
        font.setColor(Color.RED);
        font.draw(game.batch, "Defeat", 780, startY - 50);
        font.setColor(Color.WHITE);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selectedIndex++;
            if (selectedIndex > 2) selectedIndex = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            game.setScreen(new MainMenuScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedIndex == 0) game.setScreen(new MainMenuScreen(game));
            else if (selectedIndex == 1) isPvPTabActive = true;
            else if (selectedIndex == 2) isPvPTabActive = false;
        }

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        if (isMouseOver(backBounds)) selectedIndex = 0;
        else if (isMouseOver(pvpTabBounds)) selectedIndex = 1;
        else if (isMouseOver(dungeonTabBounds)) selectedIndex = 2;

        if (Gdx.input.justTouched()) {
            if (isMouseOver(backBounds)) {
                game.setScreen(new MainMenuScreen(game));
            } else if (isMouseOver(pvpTabBounds)) {
                isPvPTabActive = true;
                selectedIndex = 1;
            } else if (isMouseOver(dungeonTabBounds)) {
                isPvPTabActive = false;
                selectedIndex = 2;
            }
        }
    }

    private boolean isMouseOver(float[] bounds) {
        return mousePos.x >= bounds[0] && mousePos.x <= bounds[0] + bounds[2] &&
            mousePos.y >= bounds[1] && mousePos.y <= bounds[1] + bounds[3];
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        background.dispose();
        btnBack.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
