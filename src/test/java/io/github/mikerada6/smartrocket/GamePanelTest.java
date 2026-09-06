package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GamePanelTest {

    private static final World OPEN_WORLD = new World(200, 200, new Target(new Vec2(100, 30), 20), List.of());

    private static Simulation smallSimulation() {
        return new Simulation(new SimulationConfig(200, 200, 10, 50), OPEN_WORLD, new Random(1));
    }

    @Test
    void tickAdvancesBySpeedAndNotWhilePaused() {
        GamePanel panel = new GamePanel(smallSimulation());
        panel.tick();
        assertEquals(1, panel.simulation().age());

        panel.setStepsPerFrame(7);
        panel.tick();
        assertEquals(8, panel.simulation().age());

        panel.setPaused(true);
        panel.tick();
        assertEquals(8, panel.simulation().age());

        panel.setPaused(false);
        panel.tick();
        assertEquals(15, panel.simulation().age());
    }

    @Test
    void swappingTheSimulationResizesToItsWorld() {
        GamePanel panel = new GamePanel(smallSimulation());
        assertEquals(200, panel.getPreferredSize().width);
        Simulation bigger = new Simulation(new SimulationConfig(640, 480, 10, 50),
                new World(640, 480, new Target(new Vec2(320, 30), 20), List.of()), new Random(1));
        panel.setSimulation(bigger);
        assertEquals(640, panel.getPreferredSize().width);
        assertEquals(bigger, panel.simulation());
    }

    @Test
    void speedMustBePositive() {
        GamePanel panel = new GamePanel(smallSimulation());
        assertThrows(IllegalArgumentException.class, () -> panel.setStepsPerFrame(0));
    }
}
