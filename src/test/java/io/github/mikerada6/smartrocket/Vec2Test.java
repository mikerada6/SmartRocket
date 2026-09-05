package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class Vec2Test {

    private static final double EPS = 1e-9;

    @Test
    void normalizeProducesUnitVector() {
        Vec2 n = new Vec2(3, 4).normalize();
        assertEquals(0.6, n.x(), EPS);
        assertEquals(0.8, n.y(), EPS);
        assertEquals(1, n.mag(), EPS);
    }

    @Test
    void normalizeOfZeroVectorIsZero() {
        assertEquals(Vec2.ZERO, Vec2.ZERO.normalize());
    }

    @Test
    void setMagKeepsDirectionAndSetsMagnitude() {
        Vec2 v = new Vec2(3, 4).setMag(10);
        assertEquals(6, v.x(), EPS);
        assertEquals(8, v.y(), EPS);
        assertEquals(10, v.mag(), EPS);
    }

    @Test
    void addAndMultiplyAreComponentWise() {
        assertEquals(new Vec2(11, 22), new Vec2(1, 2).add(new Vec2(10, 20)));
        assertEquals(new Vec2(3, 6), new Vec2(1, 2).multiply(3));
    }

    @Test
    void limitClampsOnlyVectorsAboveTheLimit() {
        Vec2 clamped = new Vec2(30, 40).limit(5);
        assertEquals(3, clamped.x(), EPS);
        assertEquals(4, clamped.y(), EPS);

        Vec2 within = new Vec2(3, 4);
        assertSame(within, within.limit(10));
    }

    @Test
    void distIsEuclidean() {
        assertEquals(5, Vec2.ZERO.dist(new Vec2(3, 4)), EPS);
    }

    @Test
    void headingUsesAtan2InAllQuadrants() {
        // The original used tan(y / x), which is wrong everywhere and NaN when x is 0.
        assertEquals(0, new Vec2(1, 0).heading(), EPS);
        assertEquals(Math.PI / 2, new Vec2(0, 1).heading(), EPS);
        assertEquals(Math.PI, new Vec2(-1, 0).heading(), EPS);
        assertEquals(-Math.PI / 2, new Vec2(0, -1).heading(), EPS);
        assertEquals(Math.PI / 4, new Vec2(1, 1).heading(), EPS);
    }

    @Test
    void fromAngleRoundTripsThroughHeadingAndMag() {
        Vec2 v = Vec2.fromAngle(2.0, 4);
        assertEquals(2.0, v.heading(), EPS);
        assertEquals(4, v.mag(), EPS);
    }
}
