package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRendererTest {

    @Test
    void paintsBackgroundBarrierTargetAndRocketWhereTheModelSaysTheyAre() {
        World world = new World(200, 200, new Target(new Vec2(150, 30), 10), List.of(new Barrier(0, 100, 100, 10)));
        Simulation simulation = new Simulation(new SimulationConfig(200, 200, 1, 5), world, new Random(1));

        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        new SimulationRenderer().render(g, simulation, 60);
        g.dispose();

        assertEquals(Color.BLACK.getRGB(), image.getRGB(190, 190), "background");
        assertEquals(Color.RED.getRGB(), image.getRGB(50, 105), "barrier interior");
        assertEquals(Color.GREEN.getRGB(), image.getRGB(150, 30), "target centre");
        assertEquals(Color.GREEN.getRGB(), image.getRGB(150, 22), "target edge lies at the hit radius");
        assertEquals(Color.BLACK.getRGB(), image.getRGB(150, 5), "outside the target");

        Rocket rocket = simulation.population().getRockets().get(0);
        Vec2 pos = rocket.position();
        int rx = (int) pos.x() + Rocket.WIDTH / 2;
        int ry = (int) pos.y() + Rocket.HEIGHT / 2;
        assertEquals(rocket.color().getRGB(), image.getRGB(rx, ry), "rocket centre");
    }
}
