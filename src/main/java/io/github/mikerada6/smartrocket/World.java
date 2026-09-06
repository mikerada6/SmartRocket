package io.github.mikerada6.smartrocket;

import java.util.List;

/**
 * The static environment rockets fly through: the bounds they must stay inside, the
 * target they are trying to reach, and the barriers that destroy them on contact.
 */
public record World(int width, int height, Target target, List<Barrier> barriers) {

    public World {
        barriers = List.copyOf(barriers);
    }

    /** True when the point lies outside the playable area. */
    public boolean isOutOfBounds(Vec2 pos) {
        return pos.x() > width || pos.x() < 0 || pos.y() > height || pos.y() < 0;
    }
}
