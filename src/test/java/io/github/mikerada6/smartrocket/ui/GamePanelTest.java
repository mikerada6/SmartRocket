package io.github.mikerada6.smartrocket.ui;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.simulation.Simulation;
import io.github.mikerada6.smartrocket.simulation.SimulationConfig;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GamePanelTest {

    // Target well below the HUD text so scaled-view pixel checks are not covered by it.
    private static final World OPEN_WORLD = new World(200, 200, new Target(new Vec2(100, 120), 20), List.of());

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

    @Test
    void viewScalesToFitAndCentresTheWorld() {
        GamePanel panel = new GamePanel(smallSimulation());
        panel.setSize(200, 200);
        assertEquals(1.0, panel.viewScale(), 1e-9);

        panel.setSize(400, 100);
        assertEquals(0.5, panel.viewScale(), 1e-9, "limited by height");

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(400, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
        // The 200x200 world is drawn 100x100 wide, centred: x 150..250. The target sits at
        // world (100, 120), so at view (200, 60), and outside the world the panel background shows.
        assertEquals(SimulationRenderer.TARGET.getRGB(), image.getRGB(200, 60));
        assertEquals(SimulationRenderer.BACKGROUND.getRGB(), image.getRGB(160, 90), "inside the world");
        assertTrue(image.getRGB(20, 50) != SimulationRenderer.TARGET.getRGB(), "outside the world");
    }
}
