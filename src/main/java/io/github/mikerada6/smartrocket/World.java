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

    /** The three-barrier course from the original program, scaled to the given size. */
    public static World defaultLayout(int width, int height) {
        Target target = new Target(new Vector(width / 2, 50), 25);
        List<Barrier> barriers = List.of(
                new Barrier(0, 2 * height / 3, 7 * width / 8, 25),
                new Barrier(width - 3 * width / 4, height / 3, 3 * width / 4, 25),
                new Barrier(0, height / 8, width / 4, 25));
        return new World(width, height, target, barriers);
    }

    /** True when the point lies outside the playable area. */
    public boolean isOutOfBounds(Vector pos) {
        return pos.getX() > width || pos.getX() < 0 || pos.getY() > height || pos.getY() < 0;
    }
}
