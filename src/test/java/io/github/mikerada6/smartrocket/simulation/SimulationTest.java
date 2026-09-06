package io.github.mikerada6.smartrocket.simulation;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static final SimulationConfig SMALL = new SimulationConfig(400, 400, 50, 20);
    private static final World OPEN_WORLD = new World(400, 400, new Target(new Vec2(200, 50), 25), List.of());

    private static Simulation seeded(long seed, SimulationConfig config) {
        return new Simulation(config, OPEN_WORLD, new Random(seed));
    }

    private static void runGenerations(Simulation simulation, int generations) {
        for (int i = 0; i < generations * simulation.config().lifespan(); i++) {
            simulation.step();
        }
    }

    @Test
    void generationAdvancesExactlyEveryLifespanSteps() {
        Simulation simulation = seeded(1, SMALL);
        for (int i = 0; i < SMALL.lifespan() - 1; i++) {
            simulation.step();
        }
        assertEquals(0, simulation.generation());
        assertEquals(SMALL.lifespan() - 1, simulation.age());

        simulation.step();
        assertEquals(1, simulation.generation());
        assertEquals(0, simulation.age());
        assertEquals(SMALL.populationSize(), simulation.population().getRockets().size());
    }

    @Test
    void sameSeedGivesIdenticalRuns() {
        Simulation a = seeded(42, SMALL);
        Simulation b = seeded(42, SMALL);
        runGenerations(a, 3);
        runGenerations(b, 3);

        assertEquals(a.generation(), b.generation());
        assertEquals(a.hitsThisFrame(), b.hitsThisFrame());
        assertEquals(a.lastAverageFitness(), b.lastAverageFitness(), 0);
        List<Rocket> ra = a.population().getRockets();
        List<Rocket> rb = b.population().getRockets();
        for (int i = 0; i < ra.size(); i++) {
            assertEquals(ra.get(i).position(), rb.get(i).position());
        }
    }

    @Test
    void differentSeedsGiveDifferentRuns() {
        Simulation a = seeded(1, SMALL);
        Simulation b = seeded(2, SMALL);
        runGenerations(a, 1);
        runGenerations(b, 1);
        assertTrue(a.lastAverageFitness() != b.lastAverageFitness());
    }

    @Test
    void populationLearnsToReachTheTarget() {
        // End-to-end check that selection pressure points the right way. Before the map()
        // fix no rocket ever reached the target in 72 recorded generations.
        Simulation simulation = seeded(42, new SimulationConfig(400, 400, 200, 60));
        runGenerations(simulation, 1);
        double firstGeneration = simulation.lastAverageFitness();

        int maxHits = 0;
        for (int gen = 0; gen < 30; gen++) {
            for (int i = 0; i < simulation.config().lifespan(); i++) {
                simulation.step();
                maxHits = Math.max(maxHits, simulation.hitsThisFrame());
            }
        }
        assertTrue(maxHits > 0, "no rocket reached the target in 30 generations");
        assertTrue(simulation.lastAverageFitness() > firstGeneration,
                "average fitness did not improve: " + firstGeneration + " -> " + simulation.lastAverageFitness());
    }
    @Test
    void reportsStatsOncePerCompletedGeneration() {
        List<GenerationStats> reported = new ArrayList<>();
        Simulation simulation = new Simulation(SMALL, OPEN_WORLD, new Random(5), reported::add);
        assertNull(simulation.lastGeneration());

        runGenerations(simulation, 3);

        assertEquals(3, reported.size());
        for (int i = 0; i < 3; i++) {
            GenerationStats stats = reported.get(i);
            assertEquals(i, stats.generation());
            assertTrue(stats.maxFitness() >= stats.averageFitness());
            assertTrue(stats.hitRockets() + stats.crashedRockets() <= SMALL.populationSize());
            assertTrue(stats.hitRockets() > 0 ? stats.firstHitAge() >= 0 : stats.firstHitAge() == -1);
        }
        assertEquals(reported.get(2), simulation.lastGeneration());
        assertEquals(reported.get(2).averageFitness(), simulation.lastAverageFitness(), 0);
    }
}
