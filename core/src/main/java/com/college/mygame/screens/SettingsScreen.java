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

public class SettingsScreen extends ScreenAdapter {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;

    private Texture background;
    private Texture panelSettings;
    private Texture btnBack;

    private Texture iconSpeakerOn;
    private Texture iconSpeakerOff;
    private Texture iconMusicOn;
    private Texture iconMusicOff;

    private BitmapFont font;

    private int selectedIndex = 0;
    private final float[] backBounds = {20, MainGame.VIRTUAL_HEIGHT - 80, 60, 60};
    private final float panelWidth = 600;
    private final float panelHeight = 400;
    private final float panelX = (MainGame.VIRTUAL_WIDTH - panelWidth) / 2f;
    private final float panelY = (MainGame.VIRTUAL_HEIGHT - panelHeight) / 2f;

    private final float[] speakerBounds = {panelX + 100, panelY + 220, 60, 60};
    private final float[] musicBounds = {panelX + 100, panelY + 100, 60, 60};

    public SettingsScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();

        background = new Texture(Gdx.files.internal("backgrounds/bg_mainmenu.png"));
        panelSettings = new Texture(Gdx.files.internal("ui/panels/panel_settings.png"));
        btnBack = new Texture(Gdx.files.internal("ui/buttons/btn_back.png"));

        iconSpeakerOn = new Texture(Gdx.files.internal("ui/buttons/toggle_speaker_on.png"));
        iconSpeakerOff = new Texture(Gdx.files.internal("ui/buttons/toggle_speaker_off.png"));
        iconMusicOn = new Texture(Gdx.files.internal("ui/buttons/toggle_music_on.png"));
        iconMusicOff = new Texture(Gdx.files.internal("ui/buttons/toggle_music_off.png"));

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

        game.batch.begin();

        game.batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        game.batch.draw(background, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        game.batch.setColor(Color.WHITE);

        game.batch.draw(panelSettings, panelX, panelY, panelWidth, panelHeight);

        if (selectedIndex == 0) game.batch.setColor(Color.GOLD);
        game.batch.draw(btnBack, backBounds[0], backBounds[1], backBounds[2], backBounds[3]);
        game.batch.setColor(Color.WHITE);

        if (selectedIndex == 1) game.batch.setColor(Color.GOLD);
        Texture currentSpeaker = AudioManager.isSoundEnabled ? iconSpeakerOn : iconSpeakerOff;
        game.batch.draw(currentSpeaker, speakerBounds[0], speakerBounds[1], speakerBounds[2], speakerBounds[3]);
        game.batch.setColor(Color.WHITE);

        if (selectedIndex == 2) game.batch.setColor(Color.GOLD);
        Texture currentMusic = AudioManager.isMusicEnabled ? iconMusicOn : iconMusicOff;
        game.batch.draw(currentMusic, musicBounds[0], musicBounds[1], musicBounds[2], musicBounds[3]);
        game.batch.setColor(Color.WHITE);

        font.setColor(Color.GOLD);
        font.draw(game.batch, "SETTINGS", panelX + 250, panelY + 350);

        font.setColor(Color.WHITE);
        font.draw(game.batch, "Sound Effects Toggle  [Press S]", speakerBounds[0] + 90, speakerBounds[1] + 40);
        font.draw(game.batch, "Music Audio Toggle    [Press M]", musicBounds[0] + 90, musicBounds[1] + 40);

        font.draw(game.batch, "[MOUSE] Click to Toggle   |   [ESC/BACKSPACE] Go Back", 260, 40);

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex++;
            if (selectedIndex > 2) selectedIndex = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            AudioManager.isSoundEnabled = !AudioManager.isSoundEnabled;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            toggleMusic();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            game.setScreen(new MainMenuScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedIndex == 0) game.setScreen(new MainMenuScreen(game));
            else if (selectedIndex == 1) AudioManager.isSoundEnabled = !AudioManager.isSoundEnabled;
            else if (selectedIndex == 2) toggleMusic();
        }

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        if (isMouseOver(backBounds)) selectedIndex = 0;
        else if (isMouseOver(speakerBounds)) selectedIndex = 1;
        else if (isMouseOver(musicBounds)) selectedIndex = 2;

        if (Gdx.input.justTouched()) {
            if (isMouseOver(backBounds)) {
                game.setScreen(new MainMenuScreen(game));
            } else if (isMouseOver(speakerBounds)) {
                AudioManager.isSoundEnabled = !AudioManager.isSoundEnabled;
                selectedIndex = 1;
            } else if (isMouseOver(musicBounds)) {
                toggleMusic();
                selectedIndex = 2;
            }
        }
    }

    private void toggleMusic() {
        AudioManager.isMusicEnabled = !AudioManager.isMusicEnabled;
        if (AudioManager.isMusicEnabled) {
            AudioManager.playMusic();
        } else {
            AudioManager.stopMusic();
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
        panelSettings.dispose();
        btnBack.dispose();
        iconSpeakerOn.dispose();
        iconSpeakerOff.dispose();
        iconMusicOn.dispose();
        iconMusicOff.dispose();
        font.dispose();
    }
}
