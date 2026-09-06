package io.github.mikerada6.smartrocket.simulation;

import java.util.Random;

/**
 * Picks indices with probability proportional to non-negative weights. Replaces the
 * original mating pool, which duplicated each rocket up to 100 times in a list and
 * was rebuilt every frame. Falls back to a uniform pick when every weight is zero,
 * so selection never fails on a generation with no positive fitness.
 */
public final class WeightedPicker {

    private final double[] cumulative;
    private final Random random;

    public WeightedPicker(double[] weights, Random random) {
        if (weights.length == 0) {
            throw new IllegalArgumentException("weights must not be empty");
        }
        this.random = random;
        this.cumulative = new double[weights.length];
        double running = 0;
        for (int i = 0; i < weights.length; i++) {
            running += Math.max(0, weights[i]);
            cumulative[i] = running;
        }
    }

    /** Sum of all positive weights; zero means picks are uniform. */
    public double total() {
        return cumulative[cumulative.length - 1];
    }

    public int pick() {
        double total = total();
        if (total <= 0) {
            return random.nextInt(cumulative.length);
        }
        double x = random.nextDouble() * total;
        // First index whose cumulative weight exceeds x. x < total, so one always exists.
        int lo = 0;
        int hi = cumulative.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cumulative[mid] > x) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }
}
