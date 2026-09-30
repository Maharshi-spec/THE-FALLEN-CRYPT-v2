package com.college.mygame.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    public static boolean isSoundEnabled = true;
    public static boolean isMusicEnabled = true;

    // --- Background Music ---
    private static Music bgmMainTheme;
    private static Music bgmPvp;
    private static Music bgmDungeon;
    private static Music currentMusic;

    // --- UI Sounds ---
    private static Sound sfxButtonClick;
    private static Sound sfxPvpWinning;
    private static Sound sfxDungeonWinning;
    private static Sound sfxChestOpening;

    // --- Movement / Environment ---
    private static Sound sfxFootstepsWalking;
    private static Sound sfxSnowFootsteps;
    private static Sound sfxJump;
    private static Sound sfxFalling;

    // --- Knight Sounds ---
    private static Sound sfxSwordSlash;
    private static Sound sfxSwordStabbing;
    private static Sound sfxShieldBlock;
    private static Sound sfxKick;
    private static Sound sfxSwordAuraBlast;
    private static Sound sfxPowerStrike;

    // --- Mage Sounds ---
    private static Sound sfxFlying;
    private static Sound sfxFireball;
    private static Sound sfxShadowBinding;
    private static Sound sfxTeleportation;
    private static Sound sfxIceAgeFormation;
    private static Sound sfxThunderstorm;

    // --- Archer Sounds ---
    private static Sound sfxDaggerStab;
    private static Sound sfxArrowStrike;
    private static Sound sfxArcherAuraShot;
    private static Sound sfxArrowsRain;

    private static Sound safeLoadSound(String path) {
        try {
            if (!Gdx.files.internal(path).exists()) {
                return null;
            }
            return Gdx.audio.newSound(Gdx.files.internal(path));
        } catch (Exception e) {
            System.out.println("Audio format error for: " + path + " -> " + e.getMessage());
            return null;
        }
    }

    private static Music safeLoadMusic(String path, float volume) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Music m = Gdx.audio.newMusic(Gdx.files.internal(path));
                m.setLooping(true);
                m.setVolume(volume);
                return m;
            }
        } catch (Exception e) {
            System.out.println("Could not load music: " + path);
        }
        return null;
    }

    public static void load() {
        // Background Music
        bgmMainTheme = safeLoadMusic("audios/background music/Cyclic_Combat_maintheme.mp3", 0.45f);
        bgmPvp = safeLoadMusic("audios/background music/Crypt_Loop_pvpbg.mp3", 0.40f);
        bgmDungeon = safeLoadMusic("audios/background music/Pulse_of_Steel_dungenbg.mp3", 0.40f);

        // UI
        sfxButtonClick = safeLoadSound("audios/sound effects/button_click.mp3");
        sfxPvpWinning = safeLoadSound("audios/sound effects/pvp winning.mp3");
        sfxDungeonWinning = safeLoadSound("audios/sound effects/doungen winning.mp3");
        sfxChestOpening = safeLoadSound("audios/sound effects/Chest opening.mp3");

        // Movement & Environment (Only Footsteps walking used)
        sfxFootstepsWalking = safeLoadSound("audios/sound effects/Footsteps walking.mp3");
        sfxSnowFootsteps = safeLoadSound("audios/sound effects/snow footsteps effect.mp3");
        sfxJump = safeLoadSound("audios/sound effects/jump effect.mp3");
        sfxFalling = safeLoadSound("audios/sound effects/Falling sound.mp3");

        // Knight
        sfxSwordSlash = safeLoadSound("audios/sound effects/Sword slash.mp3");

        // Checks for .mp3 version first, falls back to .wav if you converted to 16-bit
        sfxSwordStabbing = safeLoadSound("audios/sound effects/sword stabbing sound effect.mp3");
        if (sfxSwordStabbing == null) {
            sfxSwordStabbing = safeLoadSound("audios/sound effects/sword stabbing sound effect.wav");
        }

        sfxShieldBlock = safeLoadSound("audios/sound effects/Shield block.mp3");
        sfxKick = safeLoadSound("audios/sound effects/kick sound.wav");
        sfxSwordAuraBlast = safeLoadSound("audios/sound effects/sword aura blast.wav");
        sfxPowerStrike = safeLoadSound("audios/sound effects/Power strike.mp3");

        // Mage
        sfxFlying = safeLoadSound("audios/sound effects/flying effect.wav");
        sfxFireball = safeLoadSound("audios/sound effects/Fireball.mp3");
        sfxShadowBinding = safeLoadSound("audios/sound effects/shadow binding effect.wav");
        sfxTeleportation = safeLoadSound("audios/sound effects/teleportation.wav");
        sfxIceAgeFormation = safeLoadSound("audios/sound effects/iceage formation.mp3");
        sfxThunderstorm = safeLoadSound("audios/sound effects/thunderstorm.mp3");

        // Archer
        sfxDaggerStab = safeLoadSound("audios/sound effects/dagger stab.mp3");
        sfxArrowStrike = safeLoadSound("audios/sound effects/arrow strike.wav");
        sfxArcherAuraShot = safeLoadSound("audios/sound effects/Archer aura shot.mp3");
        sfxArrowsRain = safeLoadSound("audios/sound effects/Arrows Rain.mp3");
    }

    // --- Backward-Compatibility Aliases ---
    public static void playMusic() { playMainTheme(); }
    public static void playHit() { playSwordSlash(); }
    public static void playSwing() { playSwordSlash(); }

    // --- Music Controls ---
    public static void playMainTheme() { switchMusic(bgmMainTheme); }
    public static void playPvpMusic() { switchMusic(bgmPvp); }
    public static void playDungeonMusic() { switchMusic(bgmDungeon); }

    private static void switchMusic(Music target) {
        if (currentMusic == target && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }
        stopMusic();
        currentMusic = target;
        if (isMusicEnabled && currentMusic != null) {
            currentMusic.play();
        }
    }

    public static void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) currentMusic.pause();
    }

    public static void resumeMusic() {
        if (isMusicEnabled && currentMusic != null && !currentMusic.isPlaying()) currentMusic.play();
    }

    public static void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    // --- Sound Effects Player Methods ---
    private static void playSound(Sound sfx, float volume) {
        if (isSoundEnabled && sfx != null) {
            sfx.play(volume);
        }
    }

    // UI
    public static void playButtonClick() { playSound(sfxButtonClick, 0.7f); }
    public static void playPvpWinning() { playSound(sfxPvpWinning, 0.9f); }
    public static void playDungeonWinning() { playSound(sfxDungeonWinning, 0.9f); }
    public static void playChestOpening() { playSound(sfxChestOpening, 0.8f); }

    // Movement / Environment
    public static void playFootstepsWalking() { playSound(sfxFootstepsWalking, 0.85f); }
    public static void playFootsteps2() { playFootstepsWalking(); }
    public static void playFootsteps3() { playFootstepsWalking(); } // Safely redirected
    public static void playSnowFootsteps() { playSound(sfxSnowFootsteps, 0.5f); }
    public static void playJump() { playSound(sfxJump, 0.6f); }
    public static void playFalling() { playSound(sfxFalling, 0.7f); }

    // Knight
    public static void playSwordSlash() { playSound(sfxSwordSlash, 0.7f); }
    public static void playSwordStabbing() {
        if (sfxSwordStabbing != null) playSound(sfxSwordStabbing, 0.75f);
        else playSound(sfxSwordSlash, 0.75f); // Fallback to slash if file failed
    }
    public static void playShieldBlock() { playSound(sfxShieldBlock, 0.8f); }
    public static void playKick() { playSound(sfxKick, 0.85f); }
    public static void playSwordAuraBlast() { playSound(sfxSwordAuraBlast, 0.8f); }
    public static void playPowerStrike() { playSound(sfxPowerStrike, 0.95f); }

    // Mage
    public static void playFlying() { playSound(sfxFlying, 0.6f); }
    public static void playFireball() { playSound(sfxFireball, 0.8f); }
    public static void playShadowBinding() { playSound(sfxShadowBinding, 0.85f); }
    public static void playTeleportation() { playSound(sfxTeleportation, 0.75f); }
    public static void playIceAgeFormation() { playSound(sfxIceAgeFormation, 0.9f); }
    public static void playThunderstorm() { playSound(sfxThunderstorm, 0.95f); }

    // Archer
    public static void playDaggerStab() { playSound(sfxDaggerStab, 0.7f); }
    public static void playArrowStrike() { playSound(sfxArrowStrike, 0.8f); }
    public static void playArcherAuraShot() { playSound(sfxArcherAuraShot, 0.85f); }
    public static void playArrowsRain() { playSound(sfxArrowsRain, 0.9f); }

    // Disposal
    public static void dispose() {
        if (bgmMainTheme != null) bgmMainTheme.dispose();
        if (bgmPvp != null) bgmPvp.dispose();
        if (bgmDungeon != null) bgmDungeon.dispose();

        if (sfxButtonClick != null) sfxButtonClick.dispose();
        if (sfxPvpWinning != null) sfxPvpWinning.dispose();
        if (sfxDungeonWinning != null) sfxDungeonWinning.dispose();
        if (sfxChestOpening != null) sfxChestOpening.dispose();

        if (sfxFootstepsWalking != null) sfxFootstepsWalking.dispose();
        if (sfxSnowFootsteps != null) sfxSnowFootsteps.dispose();
        if (sfxJump != null) sfxJump.dispose();
        if (sfxFalling != null) sfxFalling.dispose();

        if (sfxSwordSlash != null) sfxSwordSlash.dispose();
        if (sfxSwordStabbing != null) sfxSwordStabbing.dispose();
        if (sfxShieldBlock != null) sfxShieldBlock.dispose();
        if (sfxKick != null) sfxKick.dispose();
        if (sfxSwordAuraBlast != null) sfxSwordAuraBlast.dispose();
        if (sfxPowerStrike != null) sfxPowerStrike.dispose();

        if (sfxFlying != null) sfxFlying.dispose();
        if (sfxFireball != null) sfxFireball.dispose();
        if (sfxShadowBinding != null) sfxShadowBinding.dispose();
        if (sfxTeleportation != null) sfxTeleportation.dispose();
        if (sfxIceAgeFormation != null) sfxIceAgeFormation.dispose();
        if (sfxThunderstorm != null) sfxThunderstorm.dispose();

        if (sfxDaggerStab != null) sfxDaggerStab.dispose();
        if (sfxArrowStrike != null) sfxArrowStrike.dispose();
        if (sfxArcherAuraShot != null) sfxArcherAuraShot.dispose();
        if (sfxArrowsRain != null) sfxArrowsRain.dispose();
    }
}
