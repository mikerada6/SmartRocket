package io.github.mikerada6.smartrocket.simulation;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedPickerTest {

    @Test
    void onlyPositiveWeightIsEverPicked() {
        WeightedPicker picker = new WeightedPicker(new double[]{0, 5, 0, -3}, new Random(1));
        for (int i = 0; i < 1000; i++) {
            assertEquals(1, picker.pick());
        }
    }

    @Test
    void picksInProportionToWeight() {
        WeightedPicker picker = new WeightedPicker(new double[]{1, 3}, new Random(2));
        int[] counts = new int[2];
        for (int i = 0; i < 20_000; i++) {
            counts[picker.pick()]++;
        }
        double ratio = (double) counts[1] / counts[0];
        assertTrue(ratio > 2.7 && ratio < 3.3, "expected roughly 3:1, got " + ratio);
    }

    @Test
    void allZeroWeightsFallBackToUniform() {
        WeightedPicker picker = new WeightedPicker(new double[]{0, 0, 0}, new Random(3));
        assertEquals(0, picker.total(), 0);
        boolean[] seen = new boolean[3];
        for (int i = 0; i < 300; i++) {
            seen[picker.pick()] = true;
        }
        assertTrue(seen[0] && seen[1] && seen[2]);
    }

    @Test
    void emptyWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedPicker(new double[0], new Random()));
    }
}
