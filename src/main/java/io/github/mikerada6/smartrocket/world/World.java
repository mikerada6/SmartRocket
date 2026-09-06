package io.github.mikerada6.smartrocket.world;

import io.github.mikerada6.smartrocket.geometry.Vec2;

import java.util.List;

/**
 * The static environment rockets fly through: the bounds they must stay inside, the
 * target they are trying to reach, the barriers that destroy them on contact, and the
 * point they launch from.
 *
 * @param launch top-left corner of every rocket's collision box at the start of its life
 */
public record World(int width, int height, Target target, List<Barrier> barriers, Vec2 launch) {

    /** Size of every rocket's collision box, in world units; part of the rules, not of rendering. */
    public static final int ROCKET_WIDTH = 5;
    public static final int ROCKET_HEIGHT = 25;

    public World {
        barriers = List.copyOf(barriers);
    }

    /** A world launching from the bottom centre, as the original program did. */
    public World(int width, int height, Target target, List<Barrier> barriers) {
        this(width, height, target, barriers, defaultLaunch(width, height));
    }

    /** Bottom centre, with the rocket's full height inside the world. */
    public static Vec2 defaultLaunch(int width, int height) {
        return new Vec2(width / 2.0, height - ROCKET_HEIGHT);
    }

    public World withLaunch(Vec2 newLaunch) {
        return new World(width, height, target, barriers, newLaunch);
    }

    /** True when the point lies outside the playable area. */
    public boolean isOutOfBounds(Vec2 pos) {
        return pos.x() > width || pos.x() < 0 || pos.y() > height || pos.y() < 0;
    }
}
