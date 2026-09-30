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

public class PvpCharacterSelectScreen extends ScreenAdapter {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;

    // --- Assets ---
    private Texture background;
    private Texture btnBack;

    // Character Cards
    private Texture cardKnight;
    private Texture cardMage;
    private Texture cardArcher;

    // Player Indicators
    private Texture badgeP1;
    private Texture badgeP2;
    private Texture cursorP1;
    private Texture cursorP2;

    // Confirmation Buttons
    private Texture btnSelect;
    private Texture btnEnterActive;
    private Texture btnEnterDisabled;

    private BitmapFont font;

    // --- UI Logic & State ---

    // States: 0=P1 selecting, 1=P2 selecting, 2=Ready to start
    private int selectionState = 0;

    // Indices: 1=Knight, 2=Mage, 3=Archer
    private int highlightedIndex = 1;
    private int currentSelectedCharacter = 1; // Retains current hovered character for the SELECT button

    // Locked in indices (-1=None, 0=Knight, 1=Mage, 2=Archer)
    private int p1Character = -1;
    private int p2Character = -1;

    // 0 = Cards, 1 = Back, 2 = Enter/Select Button area
    private int focusArea = 0;

    // --- Layout Bounds (based on 960x540) ---
    private final float cardWidth = 200;
    private final float cardHeight = 300;
    private final float cardsY = 120;

    // Arrange 3 cards in center
    private float[] cardBounds0 = {(MainGame.VIRTUAL_WIDTH / 2f) - (cardWidth * 1.5f) - 40, cardsY, cardWidth, cardHeight};
    private float[] cardBounds1 = {(MainGame.VIRTUAL_WIDTH / 2f) - (cardWidth / 2f), cardsY, cardWidth, cardHeight};
    private float[] cardBounds2 = {(MainGame.VIRTUAL_WIDTH / 2f) + (cardWidth / 2f) + 40, cardsY, cardWidth, cardHeight};
    private float[][] cardBoundsArray = {cardBounds0, cardBounds1, cardBounds2};

    private float[] backBounds = {20, MainGame.VIRTUAL_HEIGHT - 80, 60, 60};
    private float[] selectBtnBounds = new float[4];
    private float[] enterBtnBounds = {(MainGame.VIRTUAL_WIDTH - 200) / 2f, 20, 200, 60};

    // Player badges below cards
    private float badgeWidth = 150;
    private float badgeHeight = 50;
    private float badgeY = cardsY - 60;
    private float[] badgeP1Bounds = {(MainGame.VIRTUAL_WIDTH / 4f) - (badgeWidth / 2f), badgeY, badgeWidth, badgeHeight};
    private float[] badgeP2Bounds = {(MainGame.VIRTUAL_WIDTH * 3 / 4f) - (badgeWidth / 2f), badgeY, badgeWidth, badgeHeight};

    public PvpCharacterSelectScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();

        // Load Assets
        background = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu.png"));
        btnBack = new Texture(Gdx.files.internal("ui/buttons/btn_back.png"));

        cardKnight = new Texture(Gdx.files.internal("ui/panels/card_select_knight.png"));
        cardMage = new Texture(Gdx.files.internal("ui/panels/card_select_mage.png"));
        cardArcher = new Texture(Gdx.files.internal("ui/panels/card_select_archer.png"));

        badgeP1 = new Texture(Gdx.files.internal("ui/buttons/badge_p1.png"));
        badgeP2 = new Texture(Gdx.files.internal("ui/buttons/badge_p2.png"));
        cursorP1 = new Texture(Gdx.files.internal("ui/panels/cursor_p1.png"));
        cursorP2 = new Texture(Gdx.files.internal("ui/panels/cursor_p2.png"));

        btnSelect = new Texture(Gdx.files.internal("ui/buttons/btn_select.png"));
        btnEnterActive = new Texture(Gdx.files.internal("ui/buttons/btn_enter_active.png"));
        btnEnterDisabled = new Texture(Gdx.files.internal("ui/buttons/btn_enter_disabled.png"));

        // Proportional sizing using native dimensions
        float selectScale = 0.32f;
        float selectW = btnSelect.getWidth() * selectScale;
        float selectH = btnSelect.getHeight() * selectScale;
        selectBtnBounds[0] = MainGame.VIRTUAL_WIDTH - selectW - 20;
        selectBtnBounds[1] = 15;
        selectBtnBounds[2] = selectW;
        selectBtnBounds[3] = selectH;

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

        // Background Dimmed
        game.batch.setColor(0.4f, 0.4f, 0.4f, 1f);
        game.batch.draw(background, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        game.batch.setColor(Color.WHITE);

        // Title
        font.setColor(Color.GOLD);
        font.draw(game.batch, "PVP: SELECT YOUR FIGHTER", 280, MainGame.VIRTUAL_HEIGHT - 30);
        font.setColor(Color.WHITE);

        // Draw Character Cards and Cursors/Highlights
        drawCard(cardKnight, 0);
        drawCard(cardMage, 1);
        drawCard(cardArcher, 2);

        game.batch.setColor(Color.WHITE);

        // Player Badges
        drawPlayerBadge(badgeP1, badgeP1Bounds, 0);
        drawPlayerBadge(badgeP2, badgeP2Bounds, 1);

        // Confirmation buttons area
        if (selectionState < 2) {
            if (focusArea == 2) game.batch.setColor(Color.GOLD);
            game.batch.draw(btnSelect, selectBtnBounds[0], selectBtnBounds[1], selectBtnBounds[2], selectBtnBounds[3]);
            game.batch.setColor(Color.WHITE);
        } else {
            if (focusArea == 2) game.batch.setColor(Color.GOLD);
            game.batch.draw(btnEnterActive, enterBtnBounds[0], enterBtnBounds[1], enterBtnBounds[2], enterBtnBounds[3]);
            game.batch.setColor(Color.WHITE);
        }

        // Back Button
        if (focusArea == 1) game.batch.setColor(Color.GOLD);
        game.batch.draw(btnBack, backBounds[0], backBounds[1], backBounds[2], backBounds[3]);
        game.batch.setColor(Color.WHITE);

        game.batch.end();
    }

    private void drawCard(Texture tex, int cardIndex) {
        float[] bounds = cardBoundsArray[cardIndex];

        if (focusArea == 0 && highlightedIndex == (cardIndex + 1)) {
            game.batch.setColor(Color.GOLD);
        } else if (focusArea == 2 && currentSelectedCharacter == (cardIndex + 1)) {
            game.batch.setColor(1.2f, 1.2f, 1.2f, 1f);
        } else {
            game.batch.setColor(Color.WHITE);
        }

        game.batch.draw(tex, bounds[0], bounds[1], bounds[2], bounds[3]);
        game.batch.setColor(Color.WHITE);

        // Cursor P1: Hovering or Locked
        if (p1Character == cardIndex) {
            game.batch.setColor(Color.GREEN);
            game.batch.draw(cursorP1, bounds[0] + (cardWidth / 2) - 30, bounds[1] + cardHeight - 10, 60, 60);
            game.batch.setColor(Color.WHITE);
        } else if (selectionState == 0 && currentSelectedCharacter == (cardIndex + 1)) {
            game.batch.draw(cursorP1, bounds[0] + (cardWidth / 2) - 30, bounds[1] + cardHeight + 20, 60, 60);
        }

        // Cursor P2: Hovering or Locked
        if (p2Character == cardIndex) {
            game.batch.setColor(Color.CYAN);
            game.batch.draw(cursorP2, bounds[0] + (cardWidth / 2) - 30, bounds[1] + cardHeight - 30, 60, 60);
            game.batch.setColor(Color.WHITE);
        } else if (selectionState == 1 && currentSelectedCharacter == (cardIndex + 1)) {
            game.batch.draw(cursorP2, bounds[0] + (cardWidth / 2) - 30, bounds[1] + cardHeight + 40, 60, 60);
        }
    }

    private void drawPlayerBadge(Texture tex, float[] bounds, int playerIndex) {
        boolean active = (selectionState == playerIndex);
        boolean locked = (playerIndex == 0 ? p1Character != -1 : p2Character != -1);

        if (active) {
            game.batch.setColor(Color.WHITE);
        } else if (locked) {
            game.batch.setColor(0.6f, 1f, 0.6f, 1f);
        } else {
            game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        }
        game.batch.draw(tex, bounds[0], bounds[1], bounds[2], bounds[3]);
        game.batch.setColor(Color.WHITE);
    }

    private void handleInput() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        boolean mouseMoving = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;

        if (mouseMoving || Gdx.input.justTouched()) {
            if (isMouseOver(backBounds)) {
                focusArea = 1;
            } else if (selectionState < 2 && isMouseOver(selectBtnBounds)) {
                focusArea = 2;
            } else if (selectionState == 2 && isMouseOver(enterBtnBounds)) {
                focusArea = 2;
            } else {
                for (int i = 0; i < 3; i++) {
                    if (isMouseOver(cardBoundsArray[i])) {
                        if (currentSelectedCharacter != (i + 1) || focusArea != 0) {
                            AudioManager.playButtonClick();
                        }
                        focusArea = 0;
                        highlightedIndex = i + 1;
                        currentSelectedCharacter = i + 1;
                        break;
                    }
                }
            }
        }

        if (Gdx.input.justTouched()) {
            if (isMouseOver(backBounds)) {
                goBack();
            } else if (selectionState < 2 && isMouseOver(selectBtnBounds)) {
                confirmPlayerSelection();
            } else if (selectionState == 2 && isMouseOver(enterBtnBounds)) {
                proceedToArena();
            } else {
                for (int i = 0; i < 3; i++) {
                    if (isMouseOver(cardBoundsArray[i])) {
                        currentSelectedCharacter = i + 1;
                        confirmPlayerSelection();
                        break;
                    }
                }
            }
        }

        // --- Keyboard Navigation ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            goBack();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            AudioManager.playButtonClick();
            if (focusArea != 0) {
                focusArea = 0;
            } else {
                currentSelectedCharacter--;
                if (currentSelectedCharacter < 1) currentSelectedCharacter = 3;
                highlightedIndex = currentSelectedCharacter;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            AudioManager.playButtonClick();
            if (focusArea != 0) {
                focusArea = 0;
            } else {
                currentSelectedCharacter++;
                if (currentSelectedCharacter > 3) currentSelectedCharacter = 1;
                highlightedIndex = currentSelectedCharacter;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            AudioManager.playButtonClick();
            focusArea--;
            if (focusArea < 0) focusArea = 2;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            AudioManager.playButtonClick();
            focusArea++;
            if (focusArea > 2) focusArea = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (focusArea == 1) {
                goBack();
            } else if (selectionState < 2) {
                confirmPlayerSelection();
            } else if (selectionState == 2) {
                proceedToArena();
            }
        }
    }

    private void confirmPlayerSelection() {
        AudioManager.playButtonClick();
        if (selectionState == 0) {
            if (currentSelectedCharacter > 0) {
                p1Character = currentSelectedCharacter - 1;
                selectionState = 1; // P2's turn to choose
                // Automatically point P2 to the next slot so they aren't completely hidden
                currentSelectedCharacter = (p1Character + 1) % 3 + 1;
                highlightedIndex = currentSelectedCharacter;
            }
        } else if (selectionState == 1) {
            if (currentSelectedCharacter > 0) {
                p2Character = currentSelectedCharacter - 1;
                selectionState = 2; // Both players locked
                focusArea = 2; // Directly highlight Enter Button
            }
        }
    }

    private boolean isMouseOver(float[] bounds) {
        return mousePos.x >= bounds[0] && mousePos.x <= bounds[0] + bounds[2] &&
            mousePos.y >= bounds[1] && mousePos.y <= bounds[1] + bounds[3];
    }

    private void goBack() {
        AudioManager.playButtonClick();
        game.setScreen(new ModeSelectScreen(game));
    }

    private void proceedToArena() {
        AudioManager.playButtonClick();
        AudioManager.stopMusic();
        game.setScreen(new PvpArenaScreen(game, p1Character, p2Character));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        background.dispose();
        btnBack.dispose();
        cardKnight.dispose();
        cardMage.dispose();
        cardArcher.dispose();
        badgeP1.dispose();
        badgeP2.dispose();
        cursorP1.dispose();
        cursorP2.dispose();
        btnSelect.dispose();
        btnEnterActive.dispose();
        btnEnterDisabled.dispose();
        font.dispose();
    }
}
