package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketTest {

    private static final int LIFESPAN = 40;
    private static final World OPEN_WORLD = new World(400, 400, new Target(new Vec2(200, 50), 25), List.of());

    /** A genome whose every gene is the same thrust vector, so the flight path is predictable. */
    private static DNA constantDna(Vec2 thrust) {
        Vec2[] genes = new Vec2[LIFESPAN];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = thrust;
        }
        return new DNA(genes, Color.WHITE, new Random(0));
    }

    private static Rocket fly(Rocket rocket) {
        for (int age = 0; age < LIFESPAN; age++) {
            rocket.update(age);
            rocket.checkBarriers();
        }
        return rocket;
    }

    private static final Vec2 UP = new Vec2(0, -Rocket.MAX_THRUST);
    private static final Vec2 RIGHT = new Vec2(Rocket.MAX_THRUST, 0);

    @Test
    void thrustingStraightUpReachesTheTargetAndStopsThere() {
        Rocket rocket = fly(new Rocket(constantDna(UP), OPEN_WORLD));
        assertTrue(rocket.hasHitTarget());
        assertFalse(rocket.hasCrashed());
        assertEquals(OPEN_WORLD.target().centre(), rocket.position());
    }

    @Test
    void leavingTheWorldCrashesAndFreezesTheRocket() {
        Rocket rocket = fly(new Rocket(constantDna(RIGHT), OPEN_WORLD));
        assertTrue(rocket.hasCrashed());
        Vec2 frozen = rocket.position();
        rocket.update(0);
        assertEquals(frozen, rocket.position());
    }

    @Test
    void touchingABarrierCrashesTheRocket() {
        World walled = new World(400, 400, OPEN_WORLD.target(), List.of(new Barrier(0, 150, 400, 100)));
        Rocket rocket = fly(new Rocket(constantDna(UP), walled));
        assertTrue(rocket.hasCrashed());
        assertFalse(rocket.hasHitTarget());
    }

    @Test
    void reachingTheTargetOutscoresMissingIt() {
        // Regression for the map() bug: a hit used to be worth roughly -20000, so this
        // comparison came out the other way and winners were bred out of the population.
        Rocket hit = fly(new Rocket(constantDna(UP), OPEN_WORLD));
        Rocket stayedHome = fly(new Rocket(constantDna(Vec2.ZERO), OPEN_WORLD));
        Rocket crashed = fly(new Rocket(constantDna(RIGHT), OPEN_WORLD));

        assertTrue(hit.calcFitness() > 0);
        assertTrue(hit.calcFitness() > stayedHome.calcFitness());
        assertTrue(stayedHome.calcFitness() > crashed.calcFitness());
    }

    @Test
    void earlierArrivalScoresHigherThanLaterArrival() {
        Rocket fast = fly(new Rocket(constantDna(UP), OPEN_WORLD));
        Rocket slow = fly(new Rocket(constantDna(UP.multiply(0.25)), OPEN_WORLD));
        assertTrue(fast.hasHitTarget());
        assertTrue(slow.hasHitTarget());
        assertTrue(fast.calcFitness() > slow.calcFitness());
    }

    @Test
    void headingFollowsVelocityAndPointsUpWhenStill() {
        Rocket rocket = new Rocket(constantDna(RIGHT), OPEN_WORLD);
        assertEquals(-Math.PI / 2, rocket.heading(), 1e-9);
        rocket.update(0);
        assertEquals(0, rocket.heading(), 1e-9);
    }
}
