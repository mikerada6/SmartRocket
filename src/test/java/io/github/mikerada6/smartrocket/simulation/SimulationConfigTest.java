package io.github.mikerada6.smartrocket.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulationConfigTest {

    @Test
    void eliteCountRoundsButNeverDropsToZeroForAPositiveFraction() {
        assertEquals(100, new SimulationConfig(10, 10, 10_000, 5, 0.01, 14, 0.01).elites());
        assertEquals(1, new SimulationConfig(10, 10, 20, 5, 0.01, 14, 0.01).elites());
        assertEquals(0, new SimulationConfig(10, 10, 20, 5, 0.01, 14, 0).elites());
        assertEquals(20, new SimulationConfig(10, 10, 20, 5, 0.01, 14, 1).elites());
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(0, 10, 10, 5));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 10, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 10, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 10, 10, 5, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 10, 10, 5, 0.01, 0, 0.01));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 10, 10, 5, 0.01, 14, -0.1));
    }
}
