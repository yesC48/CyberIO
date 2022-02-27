package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.util.Nullable;

/**
 * The interface of an individual drawable animation unit
 */
public interface IAnimated<T> {
    /**
     * Draws current frame of this animation<br/>
     * If {@code data != null}, the animation may be played based on the {@code data}.
     *
     * @param x    the central X for drawing of this
     * @param y    the central Y for drawing of this
     * @param data [Nullable] the building who has this
     */
    void draw(float x, float y, @Nullable T data);

    /**
     * Draws current frame of this animation
     *
     * @param x the central X for drawing of this
     * @param y the central Y for drawing of this
     */
    default void draw(float x, float y) {
        this.draw(x, y, null);
    }

    /**
     * Draws current frame of this animation in certain color<br/>
     * If {@code data != null}, the animation may be played based on the {@code data}.
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     * @param data  [Nullable] the object who has this
     */
    void draw(Color color, float x, float y, @Nullable T data);

    /**
     * Draws current frame of this animation in certain color
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    default void draw(Color color, float x, float y) {
        this.draw(color, x, y, null);
    }
}
