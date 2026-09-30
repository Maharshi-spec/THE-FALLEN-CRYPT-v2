package com.college.mygame.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.college.mygame.MainGame;
import com.college.mygame.utils.AnimationLoader;
import com.college.mygame.utils.AudioManager;

public class Fighter {
    public int characterType; // 0 = Knight, 1 = Mage, 2 = Archer
    public boolean isPlayer1;

    public float maxHp, currentHp;
    public float maxMp, currentMp;
    public float hpRegenRate, mpRegenRate;

    public float x, y;
    public float width = 135;
    public float height = 135;
    public boolean facingRight;

    public float velocityX = 0;
    public float velocityY = 0;
    public float speed = 280f;
    public float jumpForce = 580f;
    public float gravity = 1400f;
    public float groundY = 50f;
    public boolean isGrounded = true;

    public enum State {
        IDLE, WALK, JUMP, FALL, CROUCH, HIT, DEAD,
        // Knight
        SWORD_SWING, SWORD_STAB, SHIELD, KICK, AURA_BLAST, POWER_STRIKE,
        // Mage
        FLOAT, SKY_WALK, FIREBALL, PROTECTION, SHADOW_BIND, TELEPORT, ICE_AGE, THUNDER_STRIKE,
        // Archer
        DUAL_JUMP, DAGGER_STAB, ARROW_SHOT_STRAIGHT, ARROW_SHOT_ANGULAR, AURA_SHOT, WOOD_WALL, ARROW_RAIN
    }
    public State currentState = State.IDLE;
    public float stateTime = 0f;

    // Knight controls & combo tracking
    public boolean crouchHolding = false;
    public boolean shieldHolding = false;
    public boolean auraSpawned = false;
    public boolean powerStrikeSpawned = false;
    public boolean hasDealtMelee = false;
    public boolean hasDealtKick = false;
    public boolean isPowerStriking = false;
    public float powerStrikeSpawnTimer = 0f;
    public int powerStrikeChainCount = 0;
    public float powerStrikeStartX = 0f;
    public boolean powerStrikeFacingRight = true;
    public final int MAX_CHAIN = 7;

    // Kick tracking on opponent
    public int consecutiveKicksReceived = 0;
    public float kickComboTimer = 0f;

    // Mage controls
    public float floatTimer = 0f;
    public float skyWalkTimer = 0f;
    public boolean isProtectionHolding = false;
    public boolean fireballSpawned = false;
    public boolean bindSpawned = false;
    public boolean iceAgeSpawned = false;
    public boolean thunderSpawned = false;
    public boolean teleportPhase2 = false;

    // Archer controls
    public int jumpCount = 0;
    public boolean arrowShotSpawned = false;
    public boolean isAuraAngular = false;
    public boolean isWoodWallHolding = false;
    public boolean arrowRainSpawned = false;

    // Ice Age arena tracker
    public float walkOnIceTimer = 0f;
    public float stepSoundTimer = 0f;

    // Universal status
    public Rectangle hurtbox;
    public Rectangle meleeHitbox;
    public boolean isShielding = false;
    public boolean isBound = false;
    public float boundTimer = 0f;

    // Animations - Knight
    public Animation<TextureRegion> animIdle, animWalk, animJump, animFall, animHit, animCrouch;
    public Animation<TextureRegion> animSwordSwing, animSwordStab, animShield, animKick, animAuraBlast, animPowerStrike;
    public Animation<TextureRegion> vfxFlyingSword, vfxPowerStrikeRubble;

    // Animations - Mage
    public Animation<TextureRegion> animFloat, animFireball, animProtection, animShadowBind, animSkyWalk, animTeleport, animThunderStrike, animIceAge;
    public Animation<TextureRegion> vfxFireball, vfxIceGround, vfxShadowCube, vfxThunderUp, vfxThunderDown;

    // Animations - Archer
    public Animation<TextureRegion> animDualJump, animDaggerStab, animArrowShotStraight, animArrowShotAngular, animAuraShot, animWoodWall, animArrowRain;
    public Animation<TextureRegion> vfxArrowStraight, vfxArrowAura, vfxArrowRainUp, vfxArrowRainDown;

    public Fighter(int characterType, float startX, float startY, boolean facingRight, boolean isPlayer1) {
        this.characterType = characterType;
        this.x = startX;
        this.y = startY;
        this.groundY = startY;
        this.facingRight = facingRight;
        this.isPlayer1 = isPlayer1;

        hurtbox = new Rectangle(x + 35, y, 65, 115);
        meleeHitbox = new Rectangle(0, 0, 85, 80);

        setupStats(characterType);
        loadAssets();
    }

    private void setupStats(int type) {
        float knightHp = 1000f;
        float knightMp = 300f;

        if (type == 0) {
            maxHp = knightHp; maxMp = knightMp;
            hpRegenRate = 12f; mpRegenRate = 2f;
        } else if (type == 1) {
            maxHp = knightMp; maxMp = knightHp;
            hpRegenRate = 2f; mpRegenRate = 12f;
        } else {
            float bal = (knightHp + knightMp) / 2f;
            maxHp = bal; maxMp = bal;
            hpRegenRate = 7f; mpRegenRate = 7f;
        }
        currentHp = maxHp;
        currentMp = maxMp;
    }

    private void loadAssets() {
        String base = "characters/";
        String color = "";
        float slowFrameRate = 0.17f;

        if (characterType == 0) {
            color = isPlayer1 ? "knight/knight_red/" : "knight/knight_blue/";
            animIdle = AnimationLoader.load(base + color + "idle", slowFrameRate, Animation.PlayMode.LOOP);
            animWalk = AnimationLoader.load(base + color + "walk", slowFrameRate, Animation.PlayMode.LOOP);
            animJump = AnimationLoader.load(base + color + "jump", slowFrameRate, Animation.PlayMode.NORMAL);
            animFall = AnimationLoader.load(base + color + "fall", slowFrameRate, Animation.PlayMode.LOOP);
            animHit = AnimationLoader.load(base + color + "hit", slowFrameRate, Animation.PlayMode.NORMAL);
            animCrouch = AnimationLoader.load(base + color + "crouch", slowFrameRate, Animation.PlayMode.NORMAL);

            animSwordSwing = AnimationLoader.load(base + color + "sword_swing", slowFrameRate, Animation.PlayMode.NORMAL);
            animSwordStab = AnimationLoader.load(base + color + "sword_stab", 0.07f, Animation.PlayMode.NORMAL);
            animShield = AnimationLoader.load(base + color + "shield", slowFrameRate, Animation.PlayMode.NORMAL);
            animKick = AnimationLoader.load(base + color + "kick", slowFrameRate, Animation.PlayMode.NORMAL);
            animAuraBlast = AnimationLoader.load(base + color + "aura_blast", slowFrameRate, Animation.PlayMode.NORMAL);
            animPowerStrike = AnimationLoader.load(base + color + "power_strike", slowFrameRate, Animation.PlayMode.NORMAL);

            vfxFlyingSword = AnimationLoader.load("effects/knight/flying_sword_aura", slowFrameRate, Animation.PlayMode.LOOP);
            vfxPowerStrikeRubble = AnimationLoader.load("effects/knight/power_strike_effect", slowFrameRate, Animation.PlayMode.NORMAL);
        } else if (characterType == 1) {
            color = isPlayer1 ? "mage/mage_black/" : "mage/mage_white/";
            animIdle = AnimationLoader.load(base + color + "idle", slowFrameRate, Animation.PlayMode.LOOP);
            animWalk = AnimationLoader.load(base + color + "walk", slowFrameRate, Animation.PlayMode.LOOP);
            animFall = AnimationLoader.load(base + color + "fall", slowFrameRate, Animation.PlayMode.NORMAL);
            animHit = AnimationLoader.load(base + color + "hit", slowFrameRate, Animation.PlayMode.NORMAL);

            animFloat = AnimationLoader.load(base + color + "float", slowFrameRate, Animation.PlayMode.NORMAL);
            animFireball = AnimationLoader.load(base + color + "fireball", slowFrameRate, Animation.PlayMode.NORMAL);
            animProtection = AnimationLoader.load(base + color + "protection", slowFrameRate, Animation.PlayMode.NORMAL);
            animShadowBind = AnimationLoader.load(base + color + "shadow_bind", slowFrameRate, Animation.PlayMode.NORMAL);
            animSkyWalk = AnimationLoader.load(base + color + "sky_walk", slowFrameRate, Animation.PlayMode.LOOP);
            animTeleport = AnimationLoader.load(base + color + "teleport", slowFrameRate, Animation.PlayMode.NORMAL);
            animThunderStrike = AnimationLoader.load(base + color + "thunder_strike", slowFrameRate, Animation.PlayMode.NORMAL);
            animIceAge = AnimationLoader.load(base + color + "ice_age", slowFrameRate, Animation.PlayMode.NORMAL);

            vfxFireball = AnimationLoader.load("effects/mage/fireball_effect", slowFrameRate, Animation.PlayMode.NORMAL);
            vfxIceGround = AnimationLoader.load("effects/mage/ice_ground_effect", slowFrameRate, Animation.PlayMode.NORMAL);
            vfxShadowCube = AnimationLoader.load("effects/mage/shadow_cube_effect", slowFrameRate, Animation.PlayMode.NORMAL);
            vfxThunderUp = AnimationLoader.load("effects/mage/thunder_bolt_effect/up", 0.08f, Animation.PlayMode.NORMAL);
            vfxThunderDown = AnimationLoader.load("effects/mage/thunder_bolt_effect/down", 0.08f, Animation.PlayMode.NORMAL);
        } else {
            color = isPlayer1 ? "archer/archer_green/" : "archer/archer_yellow/";
            animIdle = AnimationLoader.load(base + color + "idle", slowFrameRate, Animation.PlayMode.LOOP);
            animWalk = AnimationLoader.load(base + color + "walk", slowFrameRate, Animation.PlayMode.LOOP);
            animJump = AnimationLoader.load(base + color + "jump", slowFrameRate, Animation.PlayMode.NORMAL);
            animFall = AnimationLoader.load(base + color + "fall", slowFrameRate, Animation.PlayMode.NORMAL);
            animHit = AnimationLoader.load(base + color + "hit", slowFrameRate, Animation.PlayMode.NORMAL);

            animDualJump = AnimationLoader.load(base + color + "dual_jump", slowFrameRate, Animation.PlayMode.NORMAL);
            animDaggerStab = AnimationLoader.load(base + color + "dagger_stab", 0.07f, Animation.PlayMode.NORMAL);
            animArrowShotStraight = AnimationLoader.load(base + color + "arrow_shot_straight", slowFrameRate, Animation.PlayMode.NORMAL);
            animArrowShotAngular = AnimationLoader.load(base + color + "arrow_shot_angular", slowFrameRate, Animation.PlayMode.NORMAL);
            animAuraShot = AnimationLoader.load(base + color + "aura_shot", slowFrameRate, Animation.PlayMode.NORMAL);
            animWoodWall = AnimationLoader.load(base + color + "wood_wall", slowFrameRate, Animation.PlayMode.NORMAL);
            animArrowRain = AnimationLoader.load(base + color + "arrow_rain", slowFrameRate, Animation.PlayMode.NORMAL);

            vfxArrowStraight = AnimationLoader.load("effects/archer/arrow_straight", slowFrameRate, Animation.PlayMode.LOOP);
            vfxArrowAura = AnimationLoader.load("effects/archer/arrow_aura", slowFrameRate, Animation.PlayMode.LOOP);
            vfxArrowRainUp = AnimationLoader.load("effects/archer/arrow_rain_wave/up", 0.08f, Animation.PlayMode.NORMAL);
            vfxArrowRainDown = AnimationLoader.load("effects/archer/arrow_rain_wave/down", 0.08f, Animation.PlayMode.NORMAL);
        }
    }

    public void update(float delta, Array<Projectile> projectiles, Array<EffectEntity> effects, Fighter opponent, boolean isIceActive) {
        if (isBound) {
            boundTimer -= delta;
            velocityX = 0;
            velocityY = 0;
            if (boundTimer <= 0) {
                isBound = false;
            }
            return;
        }

        // Decay kick combo timer
        if (kickComboTimer > 0) {
            kickComboTimer -= delta;
            if (kickComboTimer <= 0) {
                consecutiveKicksReceived = 0;
            }
        }

        stateTime += delta;

        x += velocityX * delta;
        y += velocityY * delta;

        if (y > groundY) {
            if (currentState != State.FLOAT && currentState != State.SKY_WALK) {
                velocityY -= gravity * delta;
            }
            isGrounded = false;
        } else {
            y = groundY;
            velocityY = 0;
            if (!isGrounded) {
                isGrounded = true;
                jumpCount = 0;
                AudioManager.playFootsteps2();
                if (isIceActive && characterType != 1) {
                    currentState = State.FALL;
                    stateTime = 0f;
                    velocityX = 0;
                    AudioManager.playFalling();
                } else if (currentState == State.JUMP || currentState == State.DUAL_JUMP || currentState == State.FALL) {
                    currentState = (velocityX != 0) ? State.WALK : State.IDLE;
                    stateTime = 0f;
                }
            }
        }

        if (x < -10) x = -10;
        if (x > MainGame.VIRTUAL_WIDTH - width + 10) x = MainGame.VIRTUAL_WIDTH - width + 10;

        hurtbox.set(x + 35, y, 65, (currentState == State.CROUCH) ? 65 : 115);
        if (facingRight) meleeHitbox.set(x + width - 15, y + 25, 85, 80);
        else meleeHitbox.set(x - 70, y + 25, 85, 80);

        if (currentHp > 0 && currentHp < maxHp) currentHp = Math.min(maxHp, currentHp + hpRegenRate * delta);
        if (currentMp < maxMp) currentMp = Math.min(maxMp, currentMp + mpRegenRate * delta);

        // Footstep audio timing
        if (currentState == State.WALK && isGrounded) {
            stepSoundTimer += delta;
            if (stepSoundTimer >= 0.35f) {
                stepSoundTimer = 0f;
                if (isIceActive && characterType != 1) {
                    AudioManager.playSnowFootsteps();
                } else {
                    AudioManager.playFootstepsWalking();
                }
            }
        } else {
            stepSoundTimer = 0f;
        }

        if (isIceActive && characterType != 1 && currentState == State.WALK) {
            walkOnIceTimer += delta;
            if (walkOnIceTimer >= 2.0f) {
                currentState = State.FALL;
                stateTime = 0f;
                velocityX = 0;
                walkOnIceTimer = 0f;
                AudioManager.playFalling();
            }
        } else if (currentState != State.WALK) {
            walkOnIceTimer = 0f;
        }

        if (characterType == 0) {
            updateKnightMechanics(delta, projectiles, effects, opponent);
            updatePowerStrikeChain(delta, effects, opponent);
        } else if (characterType == 1) {
            updateMageMechanics(delta, projectiles, effects, opponent);
        } else {
            updateArcherMechanics(delta, projectiles, effects, opponent);
        }

        // Check if character is actively moving horizontally on the ground
        if (Math.abs(velocityX) > 10f && isGrounded) {
            stepSoundTimer += delta;
            if (stepSoundTimer >= 0.32f) {
                stepSoundTimer = 0f;
                if (isIceActive && characterType != 1) {
                    AudioManager.playSnowFootsteps();
                } else {
                    AudioManager.playFootstepsWalking();
                }
            }
        } else {
            stepSoundTimer = 0.28f; // Pre-prime so the first step plays promptly when starting to walk
        }
    }

    private void updateKnightMechanics(float delta, Array<Projectile> projectiles, Array<EffectEntity> effects, Fighter opponent) {
        float standardFrameDur = 0.17f;

        if (currentState == State.CROUCH) {
            if (crouchHolding) {
                if (stateTime >= 4 * standardFrameDur) stateTime = 4 * standardFrameDur;
            } else {
                if (stateTime >= 8 * standardFrameDur) {
                    currentState = State.IDLE;
                    stateTime = 0f;
                }
            }
            return;
        }

        if (currentState == State.SHIELD) {
            isShielding = true;
            if (shieldHolding) {
                if (stateTime >= 5 * standardFrameDur) stateTime = 5 * standardFrameDur;
            } else {
                if (stateTime >= 8 * standardFrameDur) {
                    currentState = State.IDLE;
                    isShielding = false;
                    stateTime = 0f;
                }
            }
            return;
        }

        if (currentState == State.SWORD_STAB) {
            float stabBurstSpeed = 340f;
            float totalDuration = animSwordStab.getAnimationDuration();
            if (stateTime < totalDuration * 0.75f) {
                x += (facingRight ? 1 : -1) * stabBurstSpeed * delta;
            }
            checkMeleeHit(opponent, 140f, 0.07f * 2f);
            if (animSwordStab.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.SWORD_SWING) {
            checkMeleeHit(opponent, 110f, standardFrameDur * 2f);
            if (animSwordSwing.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        // Kick logic triggered at frame 05.png
        if (currentState == State.KICK) {
            int currentFrameIndex = (int)(stateTime / standardFrameDur);
            if (!hasDealtKick && currentFrameIndex >= 5) {
                if (meleeHitbox.overlaps(opponent.hurtbox)) {
                    hasDealtKick = true;
                    opponent.consecutiveKicksReceived++;
                    opponent.kickComboTimer = 2.0f; // 2 second rolling window

                    float pushDir = facingRight ? 1f : -1f;

                    if (opponent.consecutiveKicksReceived > 1) {
                        // Kicked more than once in 2 seconds -> Push and trigger FALL animation
                        opponent.takeDamage(90f);
                        opponent.x += pushDir * 80f; // Strong push
                        opponent.currentState = State.FALL;
                        opponent.stateTime = 0f;
                        opponent.velocityX = 0;
                        opponent.consecutiveKicksReceived = 0; // Reset combo
                        AudioManager.playFalling();
                    } else {
                        // First kick: Normal hit with push
                        opponent.takeDamage(75f);
                        opponent.x += pushDir * 40f; // Moderate push
                    }
                    AudioManager.playKick();
                }
            }
            if (animKick.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.AURA_BLAST) {
            int currentFrameIndex = (int)(stateTime / standardFrameDur);
            if (!auraSpawned && currentFrameIndex >= 5) {
                auraSpawned = true;
                float dir = facingRight ? 1 : -1;
                float spawnX = facingRight ? x + width - 10 : x - 70;
                float spawnY = y + 55;
                projectiles.add(new Projectile(spawnX, spawnY, dir * 550f, 0, 80, 50, 160f, isPlayer1, vfxFlyingSword, false));
                AudioManager.playSwordAuraBlast();
            }
            if (animAuraBlast.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.POWER_STRIKE) {
            int currentFrameIndex = (int)(stateTime / standardFrameDur);
            if (!powerStrikeSpawned && currentFrameIndex >= 6) {
                powerStrikeSpawned = true;
                isPowerStriking = true;
                powerStrikeSpawnTimer = 0f;
                powerStrikeChainCount = 0;
                powerStrikeFacingRight = facingRight;
                powerStrikeStartX = facingRight ? x + width : x - 70;
                AudioManager.playPowerStrike();
            }
            if (animPowerStrike.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.FALL && animFall.isAnimationFinished(stateTime)) {
            currentState = State.IDLE;
            stateTime = 0f;
        }

        if (currentState == State.HIT && animHit.isAnimationFinished(stateTime)) {
            currentState = State.IDLE;
            stateTime = 0f;
        }

        if (!isGrounded && currentState != State.FALL) {
            currentState = State.JUMP;
        } else if (currentState != State.HIT && currentState != State.DEAD && currentState != State.FALL) {
            if (velocityX != 0) {
                if (currentState != State.WALK) {
                    currentState = State.WALK;
                    stateTime = 0f;
                }
            } else {
                if (currentState != State.IDLE) {
                    currentState = State.IDLE;
                    stateTime = 0f;
                }
            }
        }
    }

    private void updatePowerStrikeChain(float delta, Array<EffectEntity> effects, Fighter opponent) {
        if (!isPowerStriking) return;

        powerStrikeSpawnTimer += delta;
        if (powerStrikeSpawnTimer >= 0.10f && powerStrikeChainCount < MAX_CHAIN) {
            powerStrikeSpawnTimer = 0f;
            float dir = powerStrikeFacingRight ? 1 : -1;
            float spawnX = powerStrikeStartX + (powerStrikeChainCount * dir * 65f);

            if (spawnX > 0 && spawnX < MainGame.VIRTUAL_WIDTH - 70) {
                EffectEntity rubble = new EffectEntity(spawnX, groundY, 70, 90, 60f, isPlayer1, vfxPowerStrikeRubble, true);
                effects.add(rubble);
                powerStrikeChainCount++;

                if (rubble.hitbox.overlaps(opponent.hurtbox)) {
                    opponent.takeDamage(120f);
                    rubble.hasHit = true;
                    powerStrikeChainCount = MAX_CHAIN;
                }
            } else {
                powerStrikeChainCount = MAX_CHAIN;
            }
        }

        if (powerStrikeChainCount >= MAX_CHAIN) isPowerStriking = false;
    }

    // --- MAGE UPDATE (Untouched) ---
    private void updateMageMechanics(float delta, Array<Projectile> projectiles, Array<EffectEntity> effects, Fighter opponent) {
        float fDur = 0.17f;

        if (currentState == State.FLOAT) {
            float apexY = groundY + 140f;
            if (y < apexY) {
                y = Math.min(apexY, y + 360f * delta);
            } else {
                y = apexY;
                velocityY = 0;
                floatTimer -= delta;
                if (floatTimer <= 0) {
                    currentState = State.IDLE;
                    stateTime = 0f;
                }
            }
            return;
        }

        if (currentState == State.SKY_WALK) {
            float apexY = groundY + 140f;
            y = apexY;
            velocityY = 0;
            skyWalkTimer -= delta;
            if (skyWalkTimer <= 0) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.PROTECTION) {
            isShielding = true;
            currentMp = Math.max(0, currentMp - 15f * delta);
            if (isProtectionHolding && currentMp > 0) {
                float holdLoopTime = (stateTime - 2 * fDur) % (4 * fDur);
                if (stateTime >= 2 * fDur) {
                    stateTime = 2 * fDur + holdLoopTime;
                }
            } else {
                if (stateTime >= 8 * fDur) {
                    currentState = State.IDLE;
                    isShielding = false;
                    stateTime = 0f;
                }
            }
            return;
        }

        if (currentState == State.FIREBALL) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!fireballSpawned && currentFrameIndex >= 5) {
                fireballSpawned = true;
                float dir = facingRight ? 1 : -1;
                float spawnX = facingRight ? x + width - 10 : x - 60;
                projectiles.add(new Projectile(spawnX, y + 45, dir * 500f, 0, 75, 55, 140f, isPlayer1, vfxFireball, false, true));
                AudioManager.playFireball();
            }
            if (animFireball.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.SHADOW_BIND) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!bindSpawned && currentFrameIndex >= 6) {
                bindSpawned = true;
                opponent.isBound = true;
                opponent.boundTimer = 5.0f;
                effects.add(new EffectEntity(opponent.x - 15, opponent.y - 10, 160, 160, 40f, isPlayer1, vfxShadowCube, EffectEntity.EffectType.SHADOW_BIND, 5.0f));
                AudioManager.playShadowBinding();
            }
            if (animShadowBind.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.TELEPORT) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!teleportPhase2 && currentFrameIndex >= 4) {
                teleportPhase2 = true;
                x = MainGame.VIRTUAL_WIDTH - x - width;
                facingRight = !facingRight;
                AudioManager.playTeleportation();
            }
            if (animTeleport.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.ICE_AGE) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!iceAgeSpawned && currentFrameIndex >= 6) {
                iceAgeSpawned = true;
                for (int i = 0; i < 8; i++) {
                    effects.add(new EffectEntity(i * 120, groundY - 10, 140, 60, 0, isPlayer1, vfxIceGround, EffectEntity.EffectType.ICE_AGE, 7.0f));
                }
                AudioManager.playIceAgeFormation();
            }
            if (animIceAge.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.THUNDER_STRIKE) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!thunderSpawned && currentFrameIndex >= 6) {
                thunderSpawned = true;
                effects.add(new EffectEntity(x + 25, y + 40, 80, MainGame.VIRTUAL_HEIGHT - y, 0, isPlayer1, vfxThunderUp, false));
                EffectEntity strike = new EffectEntity(opponent.x - 20, groundY, 140, 450, 220f, isPlayer1, vfxThunderDown, false);
                effects.add(strike);
                if (Math.abs((opponent.x + opponent.width / 2f) - (strike.x + strike.width / 2f)) < 90) {
                    opponent.takeDamage(220f);
                    strike.hasHit = true;
                }
                AudioManager.playThunderstorm();
            }
            if (animThunderStrike.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.HIT && animHit.isAnimationFinished(stateTime)) {
            currentState = State.IDLE;
            stateTime = 0f;
        }

        if (currentState != State.FLOAT && currentState != State.SKY_WALK && currentState != State.HIT && currentState != State.DEAD) {
            if (velocityX != 0) {
                if (currentState != State.WALK) {
                    currentState = State.WALK;
                    stateTime = 0f;
                }
            } else {
                if (currentState != State.IDLE) {
                    currentState = State.IDLE;
                    stateTime = 0f;
                }
            }
        }
    }

    // --- ARCHER UPDATE (Untouched) ---
    private void updateArcherMechanics(float delta, Array<Projectile> projectiles, Array<EffectEntity> effects, Fighter opponent) {
        float fDur = 0.17f;

        if (currentState == State.WOOD_WALL) {
            isShielding = true;
            currentMp = Math.max(0, currentMp - 15f * delta);
            if (isWoodWallHolding && currentMp > 0) {
                float holdLoopTime = (stateTime - 4 * fDur) % (4 * fDur);
                if (stateTime >= 4 * fDur) {
                    stateTime = 4 * fDur + holdLoopTime;
                }
            } else {
                if (stateTime >= 8 * fDur) {
                    currentState = State.IDLE;
                    isShielding = false;
                    stateTime = 0f;
                }
            }
            return;
        }

        if (currentState == State.DAGGER_STAB) {
            checkMeleeHit(opponent, 130f, 0.07f * 2f);
            if (animDaggerStab.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.ARROW_SHOT_STRAIGHT) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!arrowShotSpawned && currentFrameIndex >= 5) {
                arrowShotSpawned = true;
                float dir = facingRight ? 1 : -1;
                float spawnX = facingRight ? x + width - 10 : x - 60;
                projectiles.add(new Projectile(spawnX, y + 45, dir * 650f, 0, 65, 20, 110f, isPlayer1, vfxArrowStraight, false));
                AudioManager.playArrowStrike();
            }
            if (animArrowShotStraight.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.ARROW_SHOT_ANGULAR) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!arrowShotSpawned && currentFrameIndex >= 5) {
                arrowShotSpawned = true;
                float dir = facingRight ? 1 : -1;
                float spawnX = facingRight ? x + width - 10 : x - 60;
                projectiles.add(new Projectile(spawnX, y + 55, dir * 460f, 460f, 65, 20, 110f, isPlayer1, vfxArrowStraight, false));
                AudioManager.playArrowStrike();
            }
            if (animArrowShotAngular.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.AURA_SHOT) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!arrowShotSpawned && currentFrameIndex >= 5) {
                arrowShotSpawned = true;
                float dir = facingRight ? 1 : -1;
                float spawnX = facingRight ? x + width - 10 : x - 60;
                float vx = isAuraAngular ? dir * 520f : dir * 720f;
                float vy = isAuraAngular ? 520f : 0;
                projectiles.add(new Projectile(spawnX, y + 45, vx, vy, 85, 35, 175f, isPlayer1, vfxArrowAura, true));
                AudioManager.playArcherAuraShot();
            }
            Animation<TextureRegion> activeAuraAnim = isAuraAngular ? animArrowShotAngular : animAuraShot;
            if (activeAuraAnim.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.ARROW_RAIN) {
            int currentFrameIndex = (int)(stateTime / fDur);
            if (!arrowRainSpawned && currentFrameIndex >= 5) {
                arrowRainSpawned = true;
                effects.add(new EffectEntity(x + 25, y + 40, 80, MainGame.VIRTUAL_HEIGHT - y, 0, isPlayer1, vfxArrowRainUp, false));
                for (int i = 0; i < 9; i++) {
                    effects.add(new EffectEntity(i * 110f, groundY, 110, 480, 75f, isPlayer1, vfxArrowRainDown, false));
                }
                opponent.takeDamage(160f);
                AudioManager.playArrowsRain();
            }
            if (animArrowRain.isAnimationFinished(stateTime)) {
                currentState = State.IDLE;
                stateTime = 0f;
            }
            return;
        }

        if (currentState == State.DUAL_JUMP && animDualJump.isAnimationFinished(stateTime)) {
            currentState = State.JUMP;
        }

        if (currentState == State.FALL && animFall.isAnimationFinished(stateTime)) {
            currentState = State.IDLE;
            stateTime = 0f;
        }

        if (currentState == State.HIT && animHit.isAnimationFinished(stateTime)) {
            currentState = State.IDLE;
            stateTime = 0f;
        }

        if (!isGrounded && currentState != State.DUAL_JUMP && currentState != State.FALL) {
            currentState = State.JUMP;
        } else if (isGrounded && currentState != State.HIT && currentState != State.DEAD && currentState != State.FALL) {
            if (velocityX != 0) {
                if (currentState != State.WALK) {
                    currentState = State.WALK;
                    stateTime = 0f;
                }
            } else {
                if (currentState != State.IDLE) {
                    currentState = State.IDLE;
                    stateTime = 0f;
                }
            }
        }
    }

    private void checkMeleeHit(Fighter opponent, float damage, float hitWindowStart) {
        if (!hasDealtMelee && stateTime > hitWindowStart) {
            if (meleeHitbox.overlaps(opponent.hurtbox)) {
                opponent.takeDamage(damage);
                hasDealtMelee = true;
            }
        }
    }

    // Input Execution
    public void moveLeft(boolean isIceActive) {
        if (canAct()) {
            float currentSpeed = (isIceActive && characterType != 1) ? speed * 0.45f : speed;
            velocityX = -currentSpeed;
            facingRight = false;
        }
    }

    public void moveRight(boolean isIceActive) {
        if (canAct()) {
            float currentSpeed = (isIceActive && characterType != 1) ? speed * 0.45f : speed;
            velocityX = currentSpeed;
            facingRight = true;
        }
    }

    public void stopMoving() {
        velocityX = 0;
    }

    public void jumpOrFloat() {
        if (!canAct()) return;
        if (characterType == 0 && isGrounded) {
            velocityY = jumpForce;
            isGrounded = false;
            currentState = State.JUMP;
            stateTime = 0f;
            AudioManager.playJump();
        } else if (characterType == 1 && isGrounded) {
            currentState = State.FLOAT;
            stateTime = 0f;
            floatTimer = 5.0f;
            isGrounded = false;
            AudioManager.playFlying();
        } else if (characterType == 2) {
            if (isGrounded) {
                velocityY = jumpForce;
                isGrounded = false;
                jumpCount = 1;
                currentState = State.JUMP;
                stateTime = 0f;
                AudioManager.playJump();
            } else if (jumpCount == 1) {
                velocityY = jumpForce;
                jumpCount = 2;
                currentState = State.DUAL_JUMP;
                stateTime = 0f;
                AudioManager.playJump();
            }
        }
    }

    // Knight Actions
    public void crouchPress() {
        if (canAct() && isGrounded && characterType == 0) {
            currentState = State.CROUCH;
            stateTime = 0f;
            crouchHolding = true;
            velocityX = 0;
        }
    }

    public void crouchRelease() {
        if (currentState == State.CROUCH) crouchHolding = false;
    }

    public void swordSwing() {
        if (canAct()) {
            currentState = State.SWORD_SWING;
            stateTime = 0f;
            hasDealtMelee = false;
            AudioManager.playSwordSlash();
        }
    }

    public void swordStab() {
        if (canAct()) {
            currentState = State.SWORD_STAB;
            stateTime = 0f;
            hasDealtMelee = false;
            AudioManager.playSwordStabbing();
        }
    }

    public void shieldPress() {
        if (canAct() && characterType == 0) {
            currentState = State.SHIELD;
            stateTime = 0f;
            shieldHolding = true;
            isShielding = true;
            velocityX = 0;
            AudioManager.playShieldBlock();
        }
    }

    public void shieldRelease() {
        if (currentState == State.SHIELD) shieldHolding = false;
    }

    public void kick() {
        if (canAct()) {
            currentState = State.KICK;
            stateTime = 0f;
            hasDealtKick = false;
            velocityX = 0;
        }
    }

    public void auraBlast() {
        if (canAct() && currentMp >= 50) {
            currentMp -= 50;
            currentState = State.AURA_BLAST;
            stateTime = 0f;
            auraSpawned = false;
            velocityX = 0;
        }
    }

    public void powerStrike() {
        if (canAct() && currentMp >= 90) {
            currentMp -= 90;
            currentState = State.POWER_STRIKE;
            stateTime = 0f;
            powerStrikeSpawned = false;
            isPowerStriking = false;
            velocityX = 0;
        }
    }

    // Archer Actions
    public void daggerStab() {
        if (canAct()) {
            currentState = State.DAGGER_STAB;
            stateTime = 0f;
            hasDealtMelee = false;
            AudioManager.playDaggerStab();
        }
    }

    public void arrowShot(boolean isAngular) {
        if (canAct() && currentMp >= 25) {
            currentMp -= 25;
            currentState = isAngular ? State.ARROW_SHOT_ANGULAR : State.ARROW_SHOT_STRAIGHT;
            stateTime = 0f;
            arrowShotSpawned = false;
            velocityX = 0;
        }
    }

    public void auraShot(boolean isAngular) {
        if (canAct() && currentMp >= 65) {
            currentMp -= 65;
            isAuraAngular = isAngular;
            currentState = isAngular ? State.ARROW_SHOT_ANGULAR : State.AURA_SHOT;
            stateTime = 0f;
            arrowShotSpawned = false;
            velocityX = 0;
        }
    }

    public void woodWallPress() {
        if (canAct() && characterType == 2) {
            currentState = State.WOOD_WALL;
            stateTime = 0f;
            isWoodWallHolding = true;
            isShielding = true;
            velocityX = 0;
            AudioManager.playShieldBlock();
        }
    }

    public void woodWallRelease() {
        if (characterType == 2 && currentState == State.WOOD_WALL) {
            isWoodWallHolding = false;
        }
    }

    public void arrowRain() {
        if (canAct() && currentMp >= 90) {
            currentMp -= 90;
            currentState = State.ARROW_RAIN;
            stateTime = 0f;
            arrowRainSpawned = false;
            velocityX = 0;
        }
    }

    // Mage Actions
    public void skyWalk(boolean moveLeft) {
        if (characterType == 1 && (currentState == State.FLOAT || currentState == State.SKY_WALK)) {
            currentState = State.SKY_WALK;
            skyWalkTimer = 5.0f;
            velocityX = moveLeft ? -speed * 0.9f : speed * 0.9f;
            facingRight = !moveLeft;
        }
    }

    public void fireball() {
        if (canAct() && currentMp >= 40) {
            currentMp -= 40;
            currentState = State.FIREBALL;
            stateTime = 0f;
            fireballSpawned = false;
            velocityX = 0;
        }
    }

    public void protectionPress() {
        if (canAct() && characterType == 1) {
            currentState = State.PROTECTION;
            stateTime = 0f;
            isProtectionHolding = true;
            isShielding = true;
            velocityX = 0;
            AudioManager.playShieldBlock();
        }
    }

    public void protectionRelease() {
        if (characterType == 1 && currentState == State.PROTECTION) {
            isProtectionHolding = false;
        }
    }

    public void shadowBind() {
        if (canAct() && currentMp >= 70) {
            currentMp -= 70;
            currentState = State.SHADOW_BIND;
            stateTime = 0f;
            bindSpawned = false;
            velocityX = 0;
        }
    }

    public void teleport() {
        if (canAct() && currentMp >= 50) {
            currentMp -= 50;
            currentState = State.TELEPORT;
            stateTime = 0f;
            teleportPhase2 = false;
            velocityX = 0;
        }
    }

    public void iceAge() {
        if (canAct() && currentMp >= 100) {
            currentMp -= 100;
            currentState = State.ICE_AGE;
            stateTime = 0f;
            iceAgeSpawned = false;
            velocityX = 0;
        }
    }

    public void thunderStrike() {
        if (canAct() && currentMp >= 110) {
            currentMp -= 110;
            currentState = State.THUNDER_STRIKE;
            stateTime = 0f;
            thunderSpawned = false;
            velocityX = 0;
        }
    }

    public boolean canAct() {
        return !isBound && currentState != State.DEAD && currentState != State.HIT &&
            currentState != State.SWORD_SWING && currentState != State.SWORD_STAB &&
            currentState != State.SHIELD && currentState != State.KICK &&
            currentState != State.AURA_BLAST && currentState != State.POWER_STRIKE &&
            currentState != State.CROUCH && currentState != State.FIREBALL &&
            currentState != State.PROTECTION && currentState != State.SHADOW_BIND &&
            currentState != State.TELEPORT && currentState != State.ICE_AGE &&
            currentState != State.THUNDER_STRIKE && currentState != State.FALL &&
            currentState != State.DAGGER_STAB && currentState != State.ARROW_SHOT_STRAIGHT &&
            currentState != State.ARROW_SHOT_ANGULAR && currentState != State.AURA_SHOT &&
            currentState != State.WOOD_WALL && currentState != State.ARROW_RAIN;
    }

    public void takeDamage(float amount) {
        if (currentState == State.DEAD) return;
        if (isShielding) amount *= 0.2f;

        currentHp = Math.max(0, currentHp - amount);
        if (currentHp <= 0) {
            currentState = State.DEAD;
            stateTime = animHit.getAnimationDuration();
        } else {
            currentState = State.HIT;
            stateTime = 0f;
            crouchHolding = false;
            shieldHolding = false;
            isShielding = false;
            isPowerStriking = false;
            isProtectionHolding = false;
            isWoodWallHolding = false;
        }
        AudioManager.playFootsteps3();
    }

    public void draw(SpriteBatch batch) {
        Animation<TextureRegion> currentAnim = getActiveAnimation();
        if (currentAnim == null) currentAnim = animIdle;

        Object[] rawFrames = currentAnim.getKeyFrames();
        if (rawFrames == null || rawFrames.length == 0) return;
        int maxIdx = rawFrames.length - 1;

        TextureRegion frame;
        float fDur = 0.17f;

        if (characterType == 1 && (currentState == State.FLOAT || currentState == State.SKY_WALK)) {
            if (stateTime < 4 * fDur) {
                int idx = Math.min(maxIdx, (int)(stateTime / fDur));
                frame = (TextureRegion) rawFrames[idx];
            } else {
                float loopTime = (stateTime - 4 * fDur) % (5 * fDur);
                int idx = Math.min(maxIdx, 4 + (int)(loopTime / fDur));
                frame = (TextureRegion) rawFrames[idx];
            }
        } else if (characterType == 2 && currentState == State.WOOD_WALL && isWoodWallHolding) {
            float loopTime = (stateTime - 4 * fDur) % (4 * fDur);
            int idx = Math.min(maxIdx, 4 + (int)(loopTime / fDur));
            frame = (TextureRegion) rawFrames[idx];
        } else {
            boolean loop = (currentState == State.IDLE || currentState == State.WALK);
            frame = currentAnim.getKeyFrame(stateTime, loop);
        }

        if (frame != null) {
            if (frame.isFlipX() == facingRight) {
                frame.flip(true, false);
            }
            batch.draw(frame, x, y, width, height);
        }
    }

    private Animation<TextureRegion> getActiveAnimation() {
        if (characterType == 0) {
            switch (currentState) {
                case WALK: return animWalk;
                case JUMP: return animJump;
                case FALL: return animFall;
                case CROUCH: return animCrouch;
                case HIT: case DEAD: return animHit;
                case SWORD_SWING: return animSwordSwing;
                case SWORD_STAB: return animSwordStab;
                case SHIELD: return animShield;
                case KICK: return animKick;
                case AURA_BLAST: return animAuraBlast;
                case POWER_STRIKE: return animPowerStrike;
                default: return animIdle;
            }
        } else if (characterType == 1) {
            switch (currentState) {
                case WALK: return animWalk;
                case FALL: return animFall;
                case HIT: case DEAD: return animHit;
                case FLOAT: return animFloat;
                case SKY_WALK: return animSkyWalk;
                case FIREBALL: return animFireball;
                case PROTECTION: return animProtection;
                case SHADOW_BIND: return animShadowBind;
                case TELEPORT: return animTeleport;
                case ICE_AGE: return animIceAge;
                case THUNDER_STRIKE: return animThunderStrike;
                default: return animIdle;
            }
        } else {
            switch (currentState) {
                case WALK: return animWalk;
                case JUMP: return animJump;
                case DUAL_JUMP: return animDualJump;
                case FALL: return animFall;
                case HIT: case DEAD: return animHit;
                case DAGGER_STAB: return animDaggerStab;
                case ARROW_SHOT_STRAIGHT: return animArrowShotStraight;
                case ARROW_SHOT_ANGULAR: return animArrowShotAngular;
                case AURA_SHOT: return isAuraAngular ? animArrowShotAngular : animAuraShot;
                case WOOD_WALL: return animWoodWall;
                case ARROW_RAIN: return animArrowRain;
                default: return animIdle;
            }
        }
    }
}
