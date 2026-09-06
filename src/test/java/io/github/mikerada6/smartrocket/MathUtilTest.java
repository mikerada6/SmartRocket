package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilTest {

    private static final double EPS = 1e-9;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final int LIFESPAN = 200;

    @Test
    void endpointsMapToEndpoints() {
        assertEquals(10, MathUtil.map(0, 0, 100, 10, 20), EPS);
        assertEquals(20, MathUtil.map(100, 0, 100, 10, 20), EPS);
    }

    @Test
    void midpointMapsToMidpoint() {
        assertEquals(15, MathUtil.map(50, 0, 100, 10, 20), EPS);
    }

    @Test
    void nonZeroInputMinimumIsHonoured() {
        assertEquals(0, MathUtil.map(200, 200, 400, 0, 1), EPS);
        assertEquals(0.5, MathUtil.map(300, 200, 400, 0, 1), EPS);
    }

    @Test
    void reversedOutputRangeDecreasesAsInputIncreases() {
        // The fitness function maps distance-to-target onto a reversed range so that a
        // closer rocket scores higher. The original formula used the wrong denominator
        // and intercept, tripling the slope.
        assertEquals(HEIGHT, MathUtil.map(0, 0, WIDTH, HEIGHT, 0), EPS);
        assertEquals(0, MathUtil.map(WIDTH, 0, WIDTH, HEIGHT, 0), EPS);
        assertEquals(HEIGHT / 2.0, MathUtil.map(WIDTH / 2.0, 0, WIDTH, HEIGHT, 0), EPS);
    }

    @Test
    void arrivalBonusRewardsEarlyHitsAndIsNeverNegativeWithinLifespan() {
        // Regression: the original formula returned roughly -20000 for every arrival
        // time, so a rocket that reached the target was the least fit in the population.
        double early = MathUtil.map(1, 0, LIFESPAN, 20000, 0);
        double late = MathUtil.map(LIFESPAN - 1, 0, LIFESPAN, 20000, 0);
        assertTrue(early > late, "earlier arrival must earn a larger bonus");
        assertTrue(late > 0, "any arrival within the lifespan must earn a positive bonus");
        assertEquals(10000, MathUtil.map(LIFESPAN / 2.0, 0, LIFESPAN, 20000, 0), EPS);
    }

    @Test
    void extrapolatesLinearlyBeyondInputRange() {
        assertEquals(30, MathUtil.map(200, 0, 100, 10, 20), EPS);
        assertEquals(0, MathUtil.map(-100, 0, 100, 10, 20), EPS);
    }
}
