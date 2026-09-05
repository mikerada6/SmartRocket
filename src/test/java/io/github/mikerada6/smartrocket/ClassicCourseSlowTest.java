package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-size regression for ADR 0005: the default settings must solve the original course.
 * Runs 10,000 rockets for 25 generations, which takes tens of seconds, so it is excluded
 * from the default lane and run with {@code ./mvnw -Pslow test -Dgroups=slow}.
 */
@Tag("slow")
class ClassicCourseSlowTest {

    @Test
    void defaultSettingsSolveTheClassicCourse() {
        SimulationConfig config = SimulationConfig.defaults();
        Simulation simulation = new Simulation(config, CourseLayout.CLASSIC.create(config.width(), config.height()),
                new Random(1));
        int firstHitGeneration = -1;
        for (int gen = 0; gen < 25 && firstHitGeneration < 0; gen++) {
            for (int i = 0; i < config.lifespan(); i++) {
                simulation.step();
            }
            if (simulation.lastGeneration().hitRockets() > 0) {
                firstHitGeneration = gen;
            }
        }
        // Seed 1 first reaches the target at generation 9 with the defaults recorded in ADR 0005.
        assertTrue(firstHitGeneration >= 0, "no rocket reached the target on CLASSIC in 25 generations");
    }
}
