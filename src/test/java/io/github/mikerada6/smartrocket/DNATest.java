package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DNATest {

    @Test
    void everyGeneHasTheRocketThrustMagnitude() {
        DNA dna = new DNA(50, new Random(1));
        assertEquals(50, dna.length());
        for (int i = 0; i < dna.length(); i++) {
            assertEquals(Rocket.MAX_THRUST, dna.getGene(i).getMag(), 1e-9);
        }
    }

    @Test
    void sameSeedProducesSameGenome() {
        DNA a = new DNA(30, new Random(7));
        DNA b = new DNA(30, new Random(7));
        for (int i = 0; i < a.length(); i++) {
            assertEquals(a.getGene(i).getX(), b.getGene(i).getX(), 1e-12);
            assertEquals(a.getGene(i).getY(), b.getGene(i).getY(), 1e-12);
        }
        assertEquals(a.getColor(), b.getColor());
    }

    @Test
    void geneIndexWrapsAroundTheLifespan() {
        DNA dna = new DNA(10, new Random(3));
        assertSame(dna.getGene(3), dna.getGene(13));
    }

    @Test
    void crossoverTakesAPrefixFromThePartnerAndTheRestFromSelf() {
        Random random = new Random(11);
        DNA self = new DNA(20, random);
        DNA partner = new DNA(20, random);
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
