package io.github.mikerada6.smartrocket.simulation;

import io.github.mikerada6.smartrocket.geometry.Vec2;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DNATest {

    @Test
    void everyGeneHasTheRocketThrustMagnitude() {
        DNA dna = new DNA(50, SimulationConfig.DEFAULT_MUTATION_RATE, new Random(1));
        assertEquals(50, dna.length());
        for (int i = 0; i < dna.length(); i++) {
            assertEquals(Rocket.MAX_THRUST, dna.getGene(i).mag(), 1e-9);
        }
    }

    @Test
    void geneDirectionsCoverAllQuadrants() {
        // Directions are sampled by angle; the original sampled a square of random ints,
        // which biased thrust toward the diagonals.
        DNA dna = new DNA(400, SimulationConfig.DEFAULT_MUTATION_RATE, new Random(4));
        boolean[] quadrant = new boolean[4];
        for (int i = 0; i < dna.length(); i++) {
            Vec2 g = dna.getGene(i);
            quadrant[(g.x() >= 0 ? 0 : 1) + (g.y() >= 0 ? 0 : 2)] = true;
        }
        assertTrue(quadrant[0] && quadrant[1] && quadrant[2] && quadrant[3]);
    }

    @Test
    void sameSeedProducesSameGenome() {
        DNA a = new DNA(30, SimulationConfig.DEFAULT_MUTATION_RATE, new Random(7));
        DNA b = new DNA(30, SimulationConfig.DEFAULT_MUTATION_RATE, new Random(7));
        for (int i = 0; i < a.length(); i++) {
            assertEquals(a.getGene(i), b.getGene(i));
        }
    }

    @Test
    void geneIndexWrapsAroundTheLifespan() {
        DNA dna = new DNA(10, SimulationConfig.DEFAULT_MUTATION_RATE, new Random(3));
        assertSame(dna.getGene(3), dna.getGene(13));
    }

    @Test
    void crossoverTakesAPrefixFromThePartnerAndTheRestFromSelf() {
        Random random = new Random(11);
        DNA self = new DNA(20, SimulationConfig.DEFAULT_MUTATION_RATE, random);
        DNA partner = new DNA(20, SimulationConfig.DEFAULT_MUTATION_RATE, random);
        DNA child = self.crossover(partner);

        assertEquals(20, child.length());
        int switched = -1;
        for (int i = 0; i < 20; i++) {
            if (child.getGene(i) == self.getGene(i)) {
                if (switched == -1) {
                    switched = i;
                }
            } else {
                assertSame(partner.getGene(i), child.getGene(i));
                assertEquals(-1, switched, "partner genes must not appear after self genes start");
            }
        }
    }
}
