package com.college.mygame.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class EffectEntity {
    public float x, y, width, height;
    public float damage;
    public boolean isP1Owner;
    public boolean active = true;
    public float stateTime = 0f;
    public Animation<TextureRegion> animation;
    public Rectangle hitbox;
    public boolean hasHit = false;

    public boolean stayOnLastFrame = false;
    public float lingerTimer = 0f;
    public float maxLinger = 1.2f;

    public enum EffectType { STANDARD, ICE_AGE, SHADOW_BIND }
    public EffectType type = EffectType.STANDARD;
    public float effectDuration = 0f;
    private final float frameDur = 0.17f;

    public EffectEntity(float x, float y, float width, float height, float damage,
                        boolean isP1Owner, Animation<TextureRegion> animation, boolean stayOnLastFrame) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.isP1Owner = isP1Owner;
        this.animation = animation;
        this.stayOnLastFrame = stayOnLastFrame;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public EffectEntity(float x, float y, float width, float height, float damage,
                        boolean isP1Owner, Animation<TextureRegion> animation, EffectType type, float duration) {
        this(x, y, width, height, damage, isP1Owner, animation, false);
        this.type = type;
        this.effectDuration = duration;
    }

    public void update(float delta) {
        stateTime += delta;

        if (type == EffectType.ICE_AGE) {
            effectDuration -= delta;
            // Frames 07 & 08 play out after the 7-second duration expires
            if (effectDuration <= -(2 * frameDur)) {
                active = false;
            }
            return;
        }

        if (type == EffectType.SHADOW_BIND) {
            effectDuration -= delta;
            // Frames 06, 07, 08 play out after 5-second duration expires
            if (effectDuration <= -(3 * frameDur)) {
                active = false;
            }
            return;
        }

        if (animation != null && animation.isAnimationFinished(stateTime)) {
            if (stayOnLastFrame) {
                lingerTimer += delta;
                if (lingerTimer >= maxLinger) {
                    active = false;
                }
            } else {
                active = false;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (!active || animation == null) return;

        Object[] rawFrames = animation.getKeyFrames();
        if (rawFrames == null || rawFrames.length == 0) return;
        int maxIdx = rawFrames.length - 1;

        TextureRegion frame;

        if (type == EffectType.ICE_AGE) {
            if (stateTime < 6 * frameDur) {
                int idx = Math.min(maxIdx, (int)(stateTime / frameDur));
                frame = (TextureRegion) rawFrames[idx];
            } else if (effectDuration > 0) {
                // Hold on frame 06.png for 7 seconds
                int idx = Math.min(maxIdx, 6);
                frame = (TextureRegion) rawFrames[idx];
            } else {
                // Outro frames 07 and 08
                float elapsedPost = Math.abs(effectDuration);
                int idx = Math.min(maxIdx, 7 + (int)(elapsedPost / frameDur));
                frame = (TextureRegion) rawFrames[idx];
            }
        } else if (type == EffectType.SHADOW_BIND) {
            if (effectDuration > 0) {
                // Loops 02.png to 05.png during bind
                float loopTime = stateTime % (4 * frameDur);
                int offset = Math.min(3, (int)(loopTime / frameDur));
                int idx = Math.min(maxIdx, 2 + offset);
                frame = (TextureRegion) rawFrames[idx];
            } else {
                // Outro dissipation 06 to 08
                float elapsedPost = Math.abs(effectDuration);
                int idx = Math.min(maxIdx, 6 + (int)(elapsedPost / frameDur));
                frame = (TextureRegion) rawFrames[idx];
            }
        } else {
            frame = animation.getKeyFrame(stateTime, !stayOnLastFrame && false);
        }

        if (frame != null) {
            batch.draw(frame, x, y, width, height);
        }
    }
}
