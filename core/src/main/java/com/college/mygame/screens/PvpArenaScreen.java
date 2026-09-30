package com.college.mygame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.college.mygame.MainGame;
import com.college.mygame.entities.EffectEntity;
import com.college.mygame.entities.Fighter;
import com.college.mygame.entities.Projectile;
import com.college.mygame.utils.AnimationLoader;
import com.college.mygame.utils.AudioManager;

public class PvpArenaScreen extends ScreenAdapter {
    private final MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mousePos;
    private BitmapFont font;

    private enum GameState { STARTING, PLAYING, PAUSED, GAME_OVER }
    private GameState state = GameState.STARTING;
    private float stateTimer = 0f;

    private Fighter p1, p2;
    private int p1Type, p2Type;
    private float hitTimerP1 = 0f, hitTimerP2 = 0f;

    private Array<Projectile> projectiles = new Array<>();
    private Array<EffectEntity> effects = new Array<>();

    private Texture bgPvp;
    private Texture barHpFrame, barMpFrame;
    private Texture barHpFill, barMpFill;
    private Texture iconP1, iconP2;
    private Texture popupHit;
    private Texture bannerReady, bannerFight, bannerWinner, bannerDefeat;
    private Texture count3, count2, count1;
    private Texture btnPause, btnResume, btnReady, btnSettings, btnExit;
    private Animation<TextureRegion> animChest;
    private float chestTimer = 0f;
    private boolean gameOverAudioPlayed = false;

    private float[] pauseBtnBounds = {(MainGame.VIRTUAL_WIDTH - 60) / 2f, MainGame.VIRTUAL_HEIGHT - 70, 60, 60};
    private float[] menuTopBounds = {(MainGame.VIRTUAL_WIDTH - 200) / 2f, 250, 200, 60};
    private float[] menuMidBounds = {(MainGame.VIRTUAL_WIDTH - 200) / 2f, 170, 200, 60};
    private float[] menuBotBounds = {(MainGame.VIRTUAL_WIDTH - 200) / 2f, 90, 200, 60};

    public PvpArenaScreen(MainGame game, int p1Type, int p2Type) {
        this.game = game;
        this.p1Type = p1Type;
        this.p2Type = p2Type;

        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(MainGame.VIRTUAL_WIDTH / 2f, MainGame.VIRTUAL_HEIGHT / 2f, 0);
        mousePos = new Vector3();

        font = new BitmapFont();
        font.getData().setScale(1.1f);

        p1 = new Fighter(p1Type, 160, 50, true, true);
        p2 = new Fighter(p2Type, MainGame.VIRTUAL_WIDTH - 300, 50, false, false);

        bgPvp = new Texture(Gdx.files.internal("backgrounds/bg_pvp.png"));
        barHpFrame = new Texture(Gdx.files.internal("ui/hud/bar_hp_frame.png"));
        barHpFill = new Texture(Gdx.files.internal("ui/hud/bar_hp_fill.png"));
        barMpFrame = new Texture(Gdx.files.internal("ui/hud/bar_mp_frame.png"));
        barMpFill = new Texture(Gdx.files.internal("ui/hud/bar_mp_fill.png"));
        popupHit = new Texture(Gdx.files.internal("ui/hud/popup_hit.png"));

        bannerReady = new Texture(Gdx.files.internal("ui/banners/banner_ready.png"));
        bannerFight = new Texture(Gdx.files.internal("ui/banners/banner_fight.png"));
        bannerWinner = new Texture(Gdx.files.internal("ui/banners/banner_winner.png"));
        bannerDefeat = new Texture(Gdx.files.internal("ui/banners/banner_defeat.png"));

        count3 = new Texture(Gdx.files.internal("ui/hud/countdown_3.png"));
        count2 = new Texture(Gdx.files.internal("ui/hud/countdown_2.png"));
        count1 = new Texture(Gdx.files.internal("ui/hud/countdown_1.png"));

        btnPause = new Texture(Gdx.files.internal("ui/buttons/btn_pause.png"));
        btnResume = new Texture(Gdx.files.internal("ui/buttons/btn_resume.png"));
        btnReady = new Texture(Gdx.files.internal("ui/buttons/badge_ready.png"));
        btnSettings = new Texture(Gdx.files.internal("ui/buttons/btn_settings.png"));
        btnExit = new Texture(Gdx.files.internal("ui/buttons/btn_exit.png"));

        animChest = AnimationLoader.load("ui/chest", 0.17f, Animation.PlayMode.LOOP);

        iconP1 = getPortrait(p1Type);
        iconP2 = getPortrait(p2Type);
        AudioManager.playPvpMusic();
    }

    private Texture getPortrait(int type) {
        if (type == 0) return new Texture(Gdx.files.internal("ui/hud/icon_portrait_knight.png"));
        if (type == 1) return new Texture(Gdx.files.internal("ui/hud/icon_portrait_mage.png"));
        return new Texture(Gdx.files.internal("ui/hud/icon_portrait_archer.png"));
    }

    @Override
    public void render(float delta) {
        handleSystemInput();

        boolean isIceActive = checkIceAgeActive();

        if (state == GameState.PLAYING) {
            handleCombatInput(isIceActive);
            p1.update(delta, projectiles, effects, p2, isIceActive);
            p2.update(delta, projectiles, effects, p1, isIceActive);
            updateProjectiles(delta);
            checkWinCondition();
        } else if (state == GameState.GAME_OVER) {
            chestTimer += delta;
            p1.update(delta, projectiles, effects, p2, isIceActive);
            p2.update(delta, projectiles, effects, p1, isIceActive);
        } else {
            stateTimer += delta;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        if (state == GameState.PAUSED) {
            game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        } else {
            game.batch.setColor(Color.WHITE);
        }

        game.batch.draw(bgPvp, 0, 0, MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);

        for (EffectEntity e : effects) {
            if (e.type == EffectEntity.EffectType.ICE_AGE) e.draw(game.batch);
        }

        p1.draw(game.batch);
        p2.draw(game.batch);

        for (Projectile p : projectiles) p.draw(game.batch);
        for (EffectEntity e : effects) {
            if (e.type != EffectEntity.EffectType.ICE_AGE) e.draw(game.batch);
        }

        drawHUD(delta);

        game.batch.setColor(Color.WHITE);
        if (state == GameState.STARTING) drawStartSequence();
        else if (state == GameState.PAUSED) drawPauseMenu();
        else if (state == GameState.GAME_OVER) drawGameOverScreen();

        game.batch.end();
    }

    private boolean checkIceAgeActive() {
        for (EffectEntity e : effects) {
            if (e.active && e.type == EffectEntity.EffectType.ICE_AGE) return true;
        }
        return false;
    }

    private void handleCombatInput(boolean isIceActive) {
        // --- Player 1 ---
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (p1.characterType == 1 && Gdx.input.isKeyPressed(Input.Keys.W)) {
                p1.skyWalk(true);
            } else {
                p1.moveLeft(isIceActive);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (p1.characterType == 1 && Gdx.input.isKeyPressed(Input.Keys.W)) {
                p1.skyWalk(false);
            } else {
                p1.moveRight(isIceActive);
            }
        } else {
            p1.stopMoving();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) p1.crouchPress();
        else p1.crouchRelease();

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) p1.jumpOrFloat();

        if (p1.characterType == 0) { // Knight
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) p1.swordSwing();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) p1.swordStab();
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) p1.shieldPress();
            else p1.shieldRelease();
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) p1.kick();      // F = Kick
            if (Gdx.input.isKeyJustPressed(Input.Keys.V)) p1.auraBlast(); // V = Aura Blast
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) p1.powerStrike();
        } else if (p1.characterType == 1) { // Mage
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) p1.fireball();
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) p1.protectionPress();
            else p1.protectionRelease();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) p1.shadowBind();
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) p1.teleport();
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) p1.iceAge();
            if (Gdx.input.isKeyJustPressed(Input.Keys.V)) p1.thunderStrike();
        } else if (p1.characterType == 2) { // Archer
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) p1.daggerStab();
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) p1.woodWallPress();
            else p1.woodWallRelease();

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                boolean isAngular = Gdx.input.isKeyPressed(Input.Keys.W);
                p1.arrowShot(isAngular);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                boolean isAngular = Gdx.input.isKeyPressed(Input.Keys.W);
                p1.auraShot(isAngular);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) p1.arrowRain();
        }

        // --- Player 2 ---
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (p2.characterType == 1 && Gdx.input.isKeyPressed(Input.Keys.UP)) {
                p2.skyWalk(true);
            } else {
                p2.moveLeft(isIceActive);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (p2.characterType == 1 && Gdx.input.isKeyPressed(Input.Keys.UP)) {
                p2.skyWalk(false);
            } else {
                p2.moveRight(isIceActive);
            }
        } else {
            p2.stopMoving();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) p2.crouchPress();
        else p2.crouchRelease();

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) p2.jumpOrFloat();

        if (p2.characterType == 0) { // Knight
            if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) p2.shieldPress();
            else p2.shieldRelease();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) p2.swordStab();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) p2.swordSwing();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) p2.kick();      // NUMPAD_3 = Kick
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) p2.auraBlast(); // NUMPAD_5 = Aura Blast
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT) || Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
                p2.powerStrike();
            }
        } else if (p2.characterType == 1) { // Mage
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) p2.fireball();
            if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) p2.protectionPress();
            else p2.protectionRelease();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) p2.shadowBind();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) p2.teleport();
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT) || Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
                p2.iceAge();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) p2.thunderStrike(); // NUMPAD_4 = Thunder Strike
        } else if (p2.characterType == 2) { // Archer
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) p2.daggerStab();
            if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) p2.woodWallPress();
            else p2.woodWallRelease();

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                boolean isAngular = Gdx.input.isKeyPressed(Input.Keys.UP);
                p2.arrowShot(isAngular);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
                boolean isAngular = Gdx.input.isKeyPressed(Input.Keys.UP);
                p2.auraShot(isAngular);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT) || Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
                p2.arrowRain();
            }
        }
    }

    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            Fighter target = p.isP1Owner ? p2 : p1;
            if (p.active && !p.isExploding && p.hitbox.overlaps(target.hurtbox)) {
                target.takeDamage(p.damage);
                if (p.isP1Owner) hitTimerP2 = 0.45f;
                else hitTimerP1 = 0.45f;
                if (!p.piercing) {
                    p.triggerImpact();
                }
            }
            if (!p.active) projectiles.removeIndex(i);
        }

        for (int i = effects.size - 1; i >= 0; i--) {
            EffectEntity e = effects.get(i);
            e.update(delta);
            Fighter target = e.isP1Owner ? p2 : p1;
            if (e.active && !e.hasHit && e.damage > 0 && e.hitbox.overlaps(target.hurtbox)) {
                target.takeDamage(e.damage);
                e.hasHit = true;
                if (e.isP1Owner) hitTimerP2 = 0.45f;
                else hitTimerP1 = 0.45f;
            }
            if (!e.active) effects.removeIndex(i);
        }
    }

    private void checkWinCondition() {
        if (p1.currentHp <= 0 || p2.currentHp <= 0) {
            state = GameState.GAME_OVER;
            p1.velocityX = 0;
            p2.velocityX = 0;
            if (!gameOverAudioPlayed) {
                gameOverAudioPlayed = true;
                AudioManager.stopMusic();
                AudioManager.playPvpWinning();
                AudioManager.playChestOpening();
            }
        }
    }

    private void drawHUD(float delta) {
        float hudY = MainGame.VIRTUAL_HEIGHT - 80;

        game.batch.draw(iconP1, 20, hudY, 60, 60);
        float p1HpPct = p1.currentHp / p1.maxHp;
        game.batch.draw(barHpFill, 90, hudY + 35, 300 * p1HpPct, 20);
        game.batch.draw(barHpFrame, 90, hudY + 35, 300, 20);
        font.draw(game.batch, (int)p1.currentHp + "/" + (int)p1.maxHp, 200, hudY + 50);

        float p1MpPct = p1.currentMp / p1.maxMp;
        game.batch.draw(barMpFill, 90, hudY + 10, 250 * p1MpPct, 15);
        game.batch.draw(barMpFrame, 90, hudY + 10, 250, 15);
        font.draw(game.batch, (int)p1.currentMp + "/" + (int)p1.maxMp, 180, hudY + 23);

        if (hitTimerP1 > 0) {
            game.batch.draw(popupHit, 400, hudY + 25, 55, 30);
            hitTimerP1 -= delta;
        }

        game.batch.draw(iconP2, MainGame.VIRTUAL_WIDTH - 20, hudY, -60, 60);
        float p2HpPct = p2.currentHp / p2.maxHp;
        float p2HpW = 300 * p2HpPct;
        game.batch.draw(barHpFill, (MainGame.VIRTUAL_WIDTH - 90) - p2HpW, hudY + 35, p2HpW, 20);
        game.batch.draw(barHpFrame, MainGame.VIRTUAL_WIDTH - 390, hudY + 35, 300, 20);
        font.draw(game.batch, (int)p2.currentHp + "/" + (int)p2.maxHp, MainGame.VIRTUAL_WIDTH - 270, hudY + 50);

        float p2MpPct = p2.currentMp / p2.maxMp;
        float p2MpW = 250 * p2MpPct;
        game.batch.draw(barMpFill, (MainGame.VIRTUAL_WIDTH - 90) - p2MpW, hudY + 10, p2MpW, 15);
        game.batch.draw(barMpFrame, MainGame.VIRTUAL_WIDTH - 340, hudY + 10, 250, 15);
        font.draw(game.batch, (int)p2.currentMp + "/" + (int)p2.maxMp, MainGame.VIRTUAL_WIDTH - 240, hudY + 23);

        if (hitTimerP2 > 0) {
            game.batch.draw(popupHit, MainGame.VIRTUAL_WIDTH - 460, hudY + 25, 55, 30);
            hitTimerP2 -= delta;
        }

        if (state != GameState.GAME_OVER) {
            Texture pBtn = (state == GameState.PAUSED) ? btnResume : btnPause;
            game.batch.draw(pBtn, pauseBtnBounds[0], pauseBtnBounds[1], pauseBtnBounds[2], pauseBtnBounds[3]);
        }
    }

    private void drawStartSequence() {
        float cx = MainGame.VIRTUAL_WIDTH / 2f;
        float cy = MainGame.VIRTUAL_HEIGHT / 2f;
        float alpha = 1f;

        if (stateTimer <= 1.5f) {
            if (stateTimer > 1.0f) alpha = 1f - ((stateTimer - 1.0f) * 2f);
            game.batch.setColor(1, 1, 1, alpha);
            game.batch.draw(bannerReady, cx - 200, cy - 50, 400, 100);
        } else if (stateTimer <= 2.5f) {
            if (stateTimer > 2.0f) alpha = 1f - ((stateTimer - 2.0f) * 2f);
            game.batch.setColor(1, 1, 1, alpha);
            game.batch.draw(count3, cx - 50, cy - 50, 100, 100);
        } else if (stateTimer <= 3.5f) {
            if (stateTimer > 3.0f) alpha = 1f - ((stateTimer - 3.0f) * 2f);
            game.batch.setColor(1, 1, 1, alpha);
            game.batch.draw(count2, cx - 50, cy - 50, 100, 100);
        } else if (stateTimer <= 4.5f) {
            if (stateTimer > 4.0f) alpha = 1f - ((stateTimer - 4.0f) * 2f);
            game.batch.setColor(1, 1, 1, alpha);
            game.batch.draw(count1, cx - 50, cy - 50, 100, 100);
        } else if (stateTimer <= 5.5f) {
            if (stateTimer > 5.0f) alpha = 1f - ((stateTimer - 5.0f) * 2f);
            game.batch.setColor(1, 1, 1, alpha);
            game.batch.draw(bannerFight, cx - 200, cy - 50, 400, 100);
        } else {
            state = GameState.PLAYING;
        }
        game.batch.setColor(Color.WHITE);
    }

    private void drawPauseMenu() {
        game.batch.draw(btnReady, menuTopBounds[0], menuTopBounds[1], menuTopBounds[2], menuTopBounds[3]);
        game.batch.draw(btnSettings, menuMidBounds[0], menuMidBounds[1], menuMidBounds[2], menuMidBounds[3]);
        game.batch.draw(btnExit, menuBotBounds[0], menuBotBounds[1], menuBotBounds[2], menuBotBounds[3]);
        font.draw(game.batch, "GAME PAUSED", (MainGame.VIRTUAL_WIDTH / 2f) - 60, 340);
    }

    private void drawGameOverScreen() {
        float cx = MainGame.VIRTUAL_WIDTH / 2f;
        boolean p1Won = p1.currentHp > 0;
        Texture banner = p1Won ? bannerWinner : bannerDefeat;

        game.batch.draw(banner, cx - 200, 390, 400, 100);
        TextureRegion chestFrame = animChest.getKeyFrame(chestTimer, true);
        game.batch.draw(chestFrame, cx - 45, 300, 90, 90);

        game.batch.draw(btnReady, menuMidBounds[0], menuMidBounds[1], menuMidBounds[2], menuMidBounds[3]);
        game.batch.draw(btnExit, menuBotBounds[0], menuBotBounds[1], menuBotBounds[2], menuBotBounds[3]);

        font.draw(game.batch, p1Won ? "PLAYER 1 VICTORIOUS!" : "PLAYER 2 VICTORIOUS!", cx - 100, 290);
    }

    private void handleSystemInput() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (state == GameState.PLAYING) {
                state = GameState.PAUSED;
                AudioManager.pauseMusic();
            } else if (state == GameState.PAUSED) {
                state = GameState.PLAYING;
                AudioManager.resumeMusic();
            }
        }

        if (Gdx.input.justTouched()) {
            if (state == GameState.PLAYING && isMouseOver(pauseBtnBounds)) {
                state = GameState.PAUSED;
                AudioManager.pauseMusic();
                AudioManager.playButtonClick();
            }

            if (state == GameState.PAUSED) {
                if (isMouseOver(menuTopBounds)) {
                    state = GameState.PLAYING;
                    AudioManager.resumeMusic();
                    AudioManager.playButtonClick();
                } else if (isMouseOver(menuMidBounds)) {
                    AudioManager.playButtonClick();
                    game.setScreen(new SettingsScreen(game));
                } else if (isMouseOver(menuBotBounds)) {
                    AudioManager.playButtonClick();
                    AudioManager.stopMusic();
                    game.setScreen(new MainMenuScreen(game));
                }
            } else if (state == GameState.GAME_OVER) {
                if (isMouseOver(menuMidBounds)) {
                    AudioManager.playButtonClick();
                    AudioManager.stopMusic();
                    game.setScreen(new PvpArenaScreen(game, p1Type, p2Type));
                } else if (isMouseOver(menuBotBounds)) {
                    AudioManager.playButtonClick();
                    AudioManager.stopMusic();
                    game.setScreen(new PvpCharacterSelectScreen(game));
                }
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
        bgPvp.dispose();
        barHpFrame.dispose(); barHpFill.dispose();
        barMpFrame.dispose(); barMpFill.dispose();
        popupHit.dispose();
        bannerReady.dispose(); bannerFight.dispose();
        bannerWinner.dispose(); bannerDefeat.dispose();
        count3.dispose(); count2.dispose(); count1.dispose();
        btnPause.dispose(); btnResume.dispose();
        btnReady.dispose(); btnSettings.dispose(); btnExit.dispose();
        iconP1.dispose(); iconP2.dispose();
        font.dispose();
    }
}
