package com.college.mygame.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.college.mygame.MainGame;

public class Projectile {
    public float x, y;
    public float vx, vy;
    public float width, height;
    public float damage;
    public boolean active = true;
    public boolean isP1Owner;
    public boolean piercing = false;
    public float stateTime = 0f;
    public Animation<TextureRegion> animation;
    public Rectangle hitbox;

    public boolean hasImpactPhases = false;
    public boolean isExploding = false;
    public float impactTime = 0f;
    public float rotation = 0f;
    private final float frameDur = 0.17f;

    public Projectile(float x, float y, float vx, float vy, float width, float height,
                      float damage, boolean isP1Owner, Animation<TextureRegion> animation, boolean piercing) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.isP1Owner = isP1Owner;
        this.animation = animation;
        this.piercing = piercing;
        this.hitbox = new Rectangle(x, y, width, height);

        if (vy != 0) {
            rotation = (vx >= 0) ? 45f : -45f;
        }
    }

    public Projectile(float x, float y, float vx, float vy, float width, float height,
                      float damage, boolean isP1Owner, Animation<TextureRegion> animation, boolean piercing, boolean hasImpactPhases) {
        this(x, y, vx, vy, width, height, damage, isP1Owner, animation, piercing);
        this.hasImpactPhases = hasImpactPhases;
    }

    public void update(float delta) {
        stateTime += delta;

        if (hasImpactPhases && isExploding) {
            vx = 0;
            vy = 0;
            impactTime += delta;
            if (impactTime >= 3 * frameDur) {
                active = false;
            }
            return;
        }

        x += vx * delta;
        y += vy * delta;
        hitbox.setPosition(x, y);

        if (x < -150 || x > MainGame.VIRTUAL_WIDTH + 150 || y < -100 || y > MainGame.VIRTUAL_HEIGHT + 100) {
            if (hasImpactPhases && !isExploding) {
                triggerImpact();
            } else {
                active = false;
            }
        }
    }

    public void triggerImpact() {
        if (!hasImpactPhases) {
            active = false;
            return;
        }
        isExploding = true;
        impactTime = 0f;
    }

    public void draw(SpriteBatch batch) {
        if (!active || animation == null) return;

        Object[] rawFrames = animation.getKeyFrames();
        if (rawFrames == null || rawFrames.length == 0) return;
        int maxIdx = rawFrames.length - 1;

        TextureRegion frame;

        if (hasImpactPhases) {
            if (!isExploding) {
                if (stateTime < 5 * frameDur) {
                    int idx = Math.min(5, Math.min(maxIdx, (int)(stateTime / frameDur)));
                    frame = (TextureRegion) rawFrames[idx];
                } else {
                    float loopTime = (stateTime - (5 * frameDur)) % (4 * frameDur);
                    int loopOffset = (int)(loopTime / frameDur);
                    int idx = Math.min(maxIdx, 2 + Math.min(3, loopOffset));
                    frame = (TextureRegion) rawFrames[idx];
                }
            } else {
                int idx = Math.min(maxIdx, 6 + Math.min(2, (int)(impactTime / frameDur)));
                frame = (TextureRegion) rawFrames[idx];
            }
        } else {
            frame = animation.getKeyFrame(stateTime, true);
        }

        if (frame != null) {
            if ((vx < 0 && !frame.isFlipX()) || (vx > 0 && frame.isFlipX())) {
                frame.flip(true, false);
            }

            if (rotation != 0) {
                batch.draw(frame, x, y, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
            } else {
                batch.draw(frame, x, y, width, height);
            }
        }
    }
}
