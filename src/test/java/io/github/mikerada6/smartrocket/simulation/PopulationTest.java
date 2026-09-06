package io.github.mikerada6.smartrocket.simulation;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopulationTest {

    private static final World OPEN_WORLD = new World(400, 400, new Target(new Vec2(200, 50), 25), List.of());

    private static Population flown(SimulationConfig config, long seed) {
        Population population = new Population(config, OPEN_WORLD, new Random(seed));
        for (int age = 0; age < config.lifespan(); age++) {
            population.update(age);
            population.checkBarriers();
        }
        return population;
    }

    @Test
    void elitesCarryTheBestGenomesUnchangedIntoTheNextGeneration() {
        SimulationConfig config = new SimulationConfig(400, 400, 40, 30, 0.5, SimulationConfig.UNLIMITED_SPEED, 0.075);
        assertEquals(3, config.elites());
        Population population = flown(config, 8);
        population.evaluate();
        Set<DNA> bestThree = population.getRockets().stream()
                .sorted(Comparator.comparingDouble(Rocket::fitness).reversed())
                .limit(3)
                .map(Rocket::getDna)
                .collect(Collectors.toSet());

        population.selection();

        List<Rocket> next = population.getRockets();
        assertEquals(40, next.size());
        Set<DNA> carried = next.subList(0, 3).stream().map(Rocket::getDna).collect(Collectors.toSet());
        assertEquals(bestThree, carried, "the first slots must hold exactly the best genomes");
        assertEquals(3, next.stream().filter(Rocket::isElite).count());
        assertTrue(next.subList(0, 3).stream().allMatch(Rocket::isElite));
        for (Rocket r : next) {
            assertEquals(Vec2.ZERO, r.velocity(), "every rocket in the new generation starts from rest");
        }
    }

    @Test
    void withoutElitesEveryChildIsANewGenome() {
        SimulationConfig config = new SimulationConfig(400, 400, 20, 30, 0.01, SimulationConfig.UNLIMITED_SPEED, 0);
        Population population = flown(config, 8);
        Set<DNA> before = population.getRockets().stream().map(Rocket::getDna).collect(Collectors.toSet());
        population.evaluate();
        population.selection();
        for (Rocket r : population.getRockets()) {
            assertTrue(!before.contains(r.getDna()));
        }
    }

    @Test
    void selectionRequiresEvaluation() {
        Population population = new Population(new SimulationConfig(400, 400, 5, 10), OPEN_WORLD, new Random(1));
        assertThrows(IllegalStateException.class, population::selection);
    }
}
