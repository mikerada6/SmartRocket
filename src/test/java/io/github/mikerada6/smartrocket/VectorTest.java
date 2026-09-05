package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VectorTest {

    private static final double EPS = 1e-9;

    @Test
    void normalizeProducesUnitVectorWithoutNaN() {
        // Regression: normalize divided z by itself, so every 2D vector (z = 0) came out
        // with z = NaN and a NaN magnitude. Nothing visible broke only because distance
        // calculations ignore z.
        Vector n = new Vector(3, 4).normalize();
        assertEquals(0.6, n.getX(), EPS);
        assertEquals(0.8, n.getY(), EPS);
        assertEquals(0, n.getZ(), EPS);
        assertFalse(Double.isNaN(n.getZ()));
        assertEquals(1, n.getMag(), EPS);
    }

    @Test
    void normalizeOfZeroVectorIsZero() {
        Vector n = new Vector(0, 0).normalize();
        assertEquals(0, n.getX(), EPS);
        assertEquals(0, n.getY(), EPS);
        assertEquals(0, n.getMag(), EPS);
    }

    @Test
    void normalizeScalesAllThreeComponents() {
        Vector n = new Vector(0, 3, 4).normalize();
        assertEquals(0, n.getX(), EPS);
        assertEquals(0.6, n.getY(), EPS);
        assertEquals(0.8, n.getZ(), EPS);
    }

    @Test
    void setMagKeepsDirectionAndSetsMagnitude() {
        Vector v = new Vector(3, 4).setMag(10);
        assertEquals(6, v.getX(), EPS);
        assertEquals(8, v.getY(), EPS);
        assertEquals(10, v.getMag(), EPS);
    }

    @Test
    void addIsComponentWise() {
        Vector sum = new Vector(1, 2).add(new Vector(10, 20));
        assertEquals(11, sum.getX(), EPS);
        assertEquals(22, sum.getY(), EPS);
    }

    @Test
    void limitClampsOnlyVectorsAboveTheLimit() {
        // Both cases depended on getMag() not being NaN, which is why the velocity limit
        // in Rocket was commented out.
        Vector clamped = new Vector(30, 40).limit(5);
        assertEquals(5, clamped.getMag(), EPS);
        assertEquals(3, clamped.getX(), EPS);
        assertEquals(4, clamped.getY(), EPS);

        Vector untouched = new Vector(3, 4).limit(10);
        assertEquals(5, untouched.getMag(), EPS);
    }

    @Test
    void distIgnoresZAndIsEuclidean() {
        assertEquals(5, new Vector(0, 0).dist(new Vector(3, 4)), EPS);
    }
}
