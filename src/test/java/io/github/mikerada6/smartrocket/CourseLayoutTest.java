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
            assertFalse(world.isOutOfBounds(world.target().getPos()), layout + " target out of bounds");
            for (Barrier b : world.barriers()) {
                assertTrue(b.getLeft() >= 0 && b.getRight() <= WIDTH, layout + " barrier exceeds width");
                assertTrue(b.getTop() >= 0 && b.getBottom() <= HEIGHT, layout + " barrier exceeds height");
            }
        }
    }

    @Test
    void easyHasOneBarrierWithAGapAndClassicHasThree() {
        World easy = CourseLayout.EASY.create(WIDTH, HEIGHT);
        assertEquals(1, easy.barriers().size());
        assertTrue(easy.barriers().get(0).getRight() < WIDTH, "easy barrier must leave a gap");
        assertEquals(3, CourseLayout.CLASSIC.create(WIDTH, HEIGHT).barriers().size());
    }

    @Test
    void easyCourseIsSolvedWithinAFewGenerations() {
        // Pins the property that makes EASY the default: a modest population reaches the
        // target quickly, so the GUI shows learning rather than a wall of crashes.
        SimulationConfig config = new SimulationConfig(WIDTH, HEIGHT, 2000, 200);
        Simulation simulation = new Simulation(config, CourseLayout.EASY.create(WIDTH, HEIGHT), new Random(42));
        int totalHits = 0;
        for (int gen = 0; gen < 5; gen++) {
            for (int i = 0; i < config.lifespan(); i++) {
                simulation.step();
            }
            totalHits += simulation.lastGeneration().hitRockets();
        }
        assertTrue(totalHits > 0, "no rocket reached the target on the easy course in 5 generations");
    }
}
