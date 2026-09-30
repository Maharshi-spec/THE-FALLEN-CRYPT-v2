package com.college.mygame.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationLoader {

    /**
     * Loads 9 frames (00.png to 08.png) from a specified folder path.
     *
     * @param folderPath    The path to the animation folder (e.g., "characters/knight/knight_red/idle")
     * @param frameDuration How long each frame should be displayed (e.g., 0.1f for 10fps)
     * @param playMode      How the animation behaves (e.g., Animation.PlayMode.LOOP)
     * @return              A fully constructed Animation object containing the TextureRegions
     */
    public static Animation<TextureRegion> load(String folderPath, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();

        // Loop exactly 9 times for 00 to 08
        for (int i = 0; i <= 8; i++) {
            String fileName = folderPath + "/0" + i + ".png";

            // Safety check to ensure the file exists before attempting to load
            if (Gdx.files.internal(fileName).exists()) {
                Texture tex = new Texture(Gdx.files.internal(fileName));

                // We wrap the Texture in a TextureRegion so we can easily flip it left/right later
                frames.add(new TextureRegion(tex));
            } else {
                System.out.println("ERROR: Missing animation frame at: " + fileName);
            }
        }

        return new Animation<>(frameDuration, frames, playMode);
    }
}
