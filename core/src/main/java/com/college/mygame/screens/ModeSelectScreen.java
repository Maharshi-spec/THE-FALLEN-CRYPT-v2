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

public class ModeSelectScreen extends ScreenAdapter {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;

    // Textures
    private Texture background;
    private Texture cardPvp;
    private Texture cardDungeon;
    private Texture btnEnterActive;
    private Texture btnEnterDisabled;
    private Texture btnBack;

    private BitmapFont font;

    // UI Logic
    // -1 = None, 0 = PvP, 1 = Dungeon
    private int highlightedMode = -1;
    private int selectedMode = -1; // Locked in choice

    // 0 = Cards, 1 = Enter Button, 2 = Back Button (for keyboard navigation)
    private int focusArea = 0;

    // Bounds (x, y, width, height) based on 960x540
    private final float cardWidth = 250;
    private final float cardHeight = 350;
    private final float cardsY = 120;

    // Space cards apart in center
    private final float[] pvpCardBounds = {
        (MainGame.VIRTUAL_WIDTH / 2f) - cardWidth - 20, // X
        cardsY, cardWidth, cardHeight
    };
    private final float[] dungeonCardBounds = {
        (MainGame.VIRTUAL_WIDTH / 2f) + 20, // X
        cardsY, cardWidth, cardHeight
    };

    private final float[] enterBounds = {
        (MainGame.VIRTUAL_WIDTH - 200) / 2f, 30, 200, 60
    };

    private final float[] backBounds = {20, MainGame.VIRTUAL_HEIGHT - 80, 60, 60};

    public ModeSelectScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();

        // Load Assets
        background = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu.png"));
        cardPvp = new Texture(Gdx.files.internal("ui/panels/card_mode_pvp.png"));
        cardDungeon = new Texture(Gdx.files.internal("ui/panels/card_mode_dungeon.png"));
        btnEnterActive = new Texture(Gdx.files.internal("ui/buttons/btn_enter_active.png"));
        btnEnterDisabled = new Texture(Gdx.files.internal("ui/buttons/btn_enter_disabled.png"));
        btnBack = new Texture(Gdx.files.internal("ui/buttons/btn_back.png"));

        font = new BitmapFont();
        font.getData().setScale(1.5f);
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Draw background (slightly darker to focus on cards)
        game.batch.setColor(0.6f, 0.6f, 0.6f, 1f);
        game.batch.draw(background, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        game.batch.setColor(Color.WHITE);

        // Draw Title text
        font.setColor(Color.GOLD);
        font.draw(game.batch, "SELECT GAME MODE", 330, MainGame.VIRTUAL_HEIGHT - 30);
        font.setColor(Color.WHITE);

        // Draw PvP Card
        applyHighlighting(0, pvpCardBounds);
        game.batch.draw(cardPvp, pvpCardBounds[0], pvpCardBounds[1], pvpCardBounds[2], pvpCardBounds[3]);

        // Draw Dungeon Card
        applyHighlighting(1, dungeonCardBounds);
        game.batch.draw(cardDungeon, dungeonCardBounds[0], dungeonCardBounds[1], dungeonCardBounds[2], dungeonCardBounds[3]);

        game.batch.setColor(Color.WHITE); // Reset

        // Draw Enter Button (Active only if a mode is selected)
        Texture enterTex = (selectedMode != -1) ? btnEnterActive : btnEnterDisabled;

        // Keyboard focus highlighting for Enter btn
        if (focusArea == 1) game.batch.setColor(Color.GOLD);
        game.batch.draw(enterTex, enterBounds[0], enterBounds[1], enterBounds[2], enterBounds[3]);
        game.batch.setColor(Color.WHITE);

        // Draw Back Button
        if (focusArea == 2) game.batch.setColor(Color.GOLD);
        game.batch.draw(btnBack, backBounds[0], backBounds[1], backBounds[2], backBounds[3]);
        game.batch.setColor(Color.WHITE);

        // Hint text at the very bottom
        font.getData().setScale(1f);
        font.draw(game.batch, "[MOUSE] Hover & Click Cards to Select   |   [W/A/S/D] Navigate, [ENTER] Confirm", 200, 20);
        font.getData().setScale(1.5f);

        game.batch.end();
    }

    /**
     * Tints the card gold if hovered/highlighted, or green if selected.
     */
    private void applyHighlighting(int modeIndex, float[] bounds) {
        if (selectedMode == modeIndex) {
            game.batch.setColor(Color.GREEN); // Confirmed Selection
        } else if (focusArea == 0 && highlightedMode == modeIndex) {
            game.batch.setColor(Color.GOLD); // Temporary highlight
        } else {
            game.batch.setColor(Color.WHITE); // Normal
        }
    }

    private void handleInput() {
        // Unproject mouse coordinates
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        boolean mouseMoving = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;

        // --- Mouse Handling ---
        if (mouseMoving || Gdx.input.justTouched()) {
            // Check overlaps
            if (isMouseOver(pvpCardBounds)) {
                focusArea = 0;
                highlightedMode = 0;
            } else if (isMouseOver(dungeonCardBounds)) {
                focusArea = 0;
                highlightedMode = 1;
            } else if (isMouseOver(enterBounds)) {
                focusArea = 1;
            } else if (isMouseOver(backBounds)) {
                focusArea = 2;
            } else {
                // Not over anything, clear temporary highlight but keep focus area
                if (focusArea == 0) highlightedMode = -1;
            }
        }

        if (Gdx.input.justTouched()) {
            if (focusArea == 0 && highlightedMode != -1) {
                // Clicked a card, lock in selection
                selectedMode = highlightedMode;
            } else if (focusArea == 1 && selectedMode != -1) {
                // Clicked Active Enter Button
                proceedToMode();
            } else if (focusArea == 2) {
                // Clicked Back Button
                game.setScreen(new MainMenuScreen(game));
            }
        }

        // --- Keyboard Navigation ---
        // A/D or Left/Right switches between cards
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            focusArea = 0;
            if (highlightedMode == -1) highlightedMode = 1; // Default to right
            else highlightedMode = 0; // Switch to left
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            focusArea = 0;
            if (highlightedMode == -1) highlightedMode = 0; // Default to left
            else highlightedMode = 1; // Switch to right
        }

        // W/S or Up/Down shifts focus vertically (Back -> Cards -> Enter)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            focusArea--;
            if (focusArea < 0) focusArea = 2; // Wrap to Back Button
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            focusArea++;
            if (focusArea > 2) focusArea = 0; // Wrap to Cards
        }

        // Special mapping for ESC to go back directly
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }

        // Enter confirms current focused action
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (focusArea == 0 && highlightedMode != -1) {
                selectedMode = highlightedMode; // Lock selection via keyboard
            } else if (focusArea == 1 && selectedMode != -1) {
                proceedToMode();
            } else if (focusArea == 2) {
                game.setScreen(new MainMenuScreen(game));
            }
        }
    }

    private boolean isMouseOver(float[] bounds) {
        return mousePos.x >= bounds[0] && mousePos.x <= bounds[0] + bounds[2] &&
            mousePos.y >= bounds[1] && mousePos.y <= bounds[1] + bounds[3];
    }

    private void proceedToMode() {
        if (selectedMode == 0) {
            System.out.println("Entering PvP Setup...");
            game.setScreen(new PvpCharacterSelectScreen(game)); // We build this later
        } else if (selectedMode == 1) {
            System.out.println("Entering Dungeon Setup (Deferred)...");
            // Placeholder: Dungeon is not part of immediate PvP target
            // game.setScreen(new DungeonPartySizeScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        background.dispose();
        cardPvp.dispose();
        cardDungeon.dispose();
        btnEnterActive.dispose();
        btnEnterDisabled.dispose();
        btnBack.dispose();
        font.dispose();
    }
}
