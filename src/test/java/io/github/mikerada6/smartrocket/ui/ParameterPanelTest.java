package io.github.mikerada6.smartrocket.ui;

import io.github.mikerada6.smartrocket.simulation.SimulationConfig;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterPanelTest {

    @Test
    void applyHandsOverTheEditedConfiguration() {
        AtomicReference<SimulationConfig> applied = new AtomicReference<>();
        ParameterPanel panel = new ParameterPanel(SimulationConfig.defaults(), applied::set);
        assertEquals(SimulationConfig.defaults(), panel.value(), "spinners start at the given values");

        panel.populationSpinner().setValue(2500);
        panel.elitePercentSpinner().setValue(2.0);
        assertNull(applied.get(), "nothing applied until the button is pressed");
        panel.applyButton().doClick();

        SimulationConfig c = applied.get();
        assertEquals(2500, c.populationSize());
        assertEquals(0.02, c.eliteFraction(), 1e-12);
        assertEquals(SimulationConfig.DEFAULT_MAX_SPEED, c.maxSpeed(), 0);
        assertEquals(c, panel.current());
    }

    @Test
    void uncheckingLimitSpeedMeansUnlimited() {
        AtomicReference<SimulationConfig> applied = new AtomicReference<>();
        ParameterPanel panel = new ParameterPanel(SimulationConfig.defaults(), applied::set);
        panel.limitSpeedBox().doClick();
        panel.applyButton().doClick();
        assertEquals(SimulationConfig.UNLIMITED_SPEED, applied.get().maxSpeed(), 0);
    }

    @Test
    void worldSizeFollowsTheCourseWithoutTouchingTheSpinners() {
        ParameterPanel panel = new ParameterPanel(SimulationConfig.defaults(), c -> { });
        panel.populationSpinner().setValue(77);
        panel.setWorldSize(640, 480);
        assertEquals(640, panel.current().width());
        assertEquals(10_000, panel.current().populationSize(), "unapplied edits stay staged");
        assertEquals(77, panel.value().populationSize());
        assertEquals(640, panel.value().width());
    }

    @Test
    void invalidCombinationsShowAnErrorInsteadOfApplying() {
        AtomicReference<SimulationConfig> applied = new AtomicReference<>();
        ParameterPanel panel = new ParameterPanel(new SimulationConfig(100, 100, 10, 10), applied::set);
        // The spinner models prevent most bad input, so force a value the record rejects.
        panel.elitePercentSpinner().setValue(100.0);
        panel.populationSpinner().setValue(1);
        panel.applyButton().doClick();
        assertTrue(applied.get() != null, "one elite of one rocket is valid");
        assertEquals(" ", panel.errorText());
    }
}
