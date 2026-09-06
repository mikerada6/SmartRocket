package io.github.mikerada6.smartrocket;

import java.util.List;

/** Named barrier courses. Each builds a {@link World} scaled to the requested size. */
public enum CourseLayout {

    /**
     * One barrier across the left three quarters at mid height, so rockets must steer
     * right and back to reach the target. A random population of 10,000 already lands
     * some hits in its first generation, which makes it the default while the genetic
     * algorithm is being reworked.
     */
    EASY {
        @Override
        public World create(int width, int height) {
            return new World(width, height, target(width),
                    List.of(new Barrier(0, height / 2, 3 * width / 4, 25)));
        }
    },

    /**
     * The original three-barrier course. The current algorithm cannot solve it: every
     * rocket crashes, so it is kept as the benchmark for algorithm improvements rather
     * than as the default.
     */
    CLASSIC {
        @Override
        public World create(int width, int height) {
            return new World(width, height, target(width), List.of(
                    new Barrier(0, 2 * height / 3, 7 * width / 8, 25),
                    new Barrier(width - 3 * width / 4, height / 3, 3 * width / 4, 25),
                    new Barrier(0, height / 8, width / 4, 25)));
        }
    };

    public abstract World create(int width, int height);

    private static Target target(int width) {
        return new Target(new Vec2(width / 2, 50), 25);
    }
}
