package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseLayoutTest {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    @Test
    void everyLayoutKeepsTargetAndBarriersInsideTheWorld() {
        for (CourseLayout layout : CourseLayout.values()) {
            World world = layout.create(WIDTH, HEIGHT);
            assertFalse(world.isOutOfBounds(world.target().centre()), layout + " target out of bounds");
            for (Barrier b : world.barriers()) {
                assertTrue(b.left() >= 0 && b.right() <= WIDTH, layout + " barrier exceeds width");
                assertTrue(b.top() >= 0 && b.bottom() <= HEIGHT, layout + " barrier exceeds height");
            }
        }
    }

    @Test
    void easyHasOneBarrierWithAGapAndClassicHasThree() {
        World easy = CourseLayout.EASY.create(WIDTH, HEIGHT);
        assertEquals(1, easy.barriers().size());
        assertTrue(easy.barriers().get(0).right() < WIDTH, "easy barrier must leave a gap");
        assertEquals(3, CourseLayout.CLASSIC.create(WIDTH, HEIGHT).barriers().size());
    }

    @Test
    void easyCourseIsLargelySolvedWithinTwentyGenerations() {
        // Pins the property that makes EASY the default: a modest population reaches the
        // target quickly, so the GUI shows learning rather than a wall of crashes. The first
        // hit can take up to eight generations depending on the seed, but across seventeen
        // measured seeds the twentieth generation had 31% to 64% of rockets on target, so
        // a quarter leaves a margin without being trivially satisfied.
        SimulationConfig config = new SimulationConfig(WIDTH, HEIGHT, 2000, 200);
        Simulation simulation = new Simulation(config, CourseLayout.EASY.create(WIDTH, HEIGHT), new Random(42));
        for (int gen = 0; gen < 20; gen++) {
            for (int i = 0; i < config.lifespan(); i++) {
                simulation.step();
            }
        }
        int hits = simulation.lastGeneration().hitRockets();
        assertTrue(hits > config.populationSize() / 4,
                "only " + hits + " of " + config.populationSize() + " rockets on target after 20 generations");
    }
}
