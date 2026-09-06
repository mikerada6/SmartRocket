package io.github.mikerada6.smartrocket.ui;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.simulation.DNA;
import io.github.mikerada6.smartrocket.simulation.Rocket;
import io.github.mikerada6.smartrocket.simulation.Simulation;
import io.github.mikerada6.smartrocket.simulation.SimulationConfig;
import io.github.mikerada6.smartrocket.world.Barrier;
import io.github.mikerada6.smartrocket.world.CourseLayout;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

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

        assertEquals(SimulationRenderer.BACKGROUND.getRGB(), image.getRGB(190, 190), "background");
        assertEquals(SimulationRenderer.BARRIER.getRGB(), image.getRGB(50, 105), "barrier interior");
        assertEquals(SimulationRenderer.TARGET.getRGB(), image.getRGB(150, 30), "target centre");
        assertEquals(SimulationRenderer.TARGET.getRGB(), image.getRGB(150, 22), "target edge lies at the hit radius");
        assertEquals(SimulationRenderer.BACKGROUND.getRGB(), image.getRGB(150, 5), "outside the target");

        Rocket rocket = simulation.population().getRockets().get(0);
        Vec2 pos = rocket.position();
        int rx = (int) pos.x() + World.ROCKET_WIDTH / 2;
        int ry = (int) pos.y() + World.ROCKET_HEIGHT / 2;
        assertEquals(SimulationRenderer.colorFor(rocket, world).getRGB(), image.getRGB(rx, ry), "rocket centre");
    }

    @Test
    void rocketColourEncodesStateAndDistance() {
        World world = new World(400, 400, new Target(new Vec2(200, 50), 25), List.of());
        Vec2[] up = new Vec2[40];
        java.util.Arrays.fill(up, new Vec2(0, -Rocket.MAX_THRUST));
        Vec2[] right = new Vec2[40];
        java.util.Arrays.fill(right, new Vec2(Rocket.MAX_THRUST, 0));
        Random random = new Random(1);
        double rate = SimulationConfig.DEFAULT_MUTATION_RATE;

        Rocket still = new Rocket(new DNA(up, rate, random), world);
        Color far = SimulationRenderer.colorFor(still, world);
        Rocket flying = new Rocket(new DNA(up, rate, random), world);
        for (int age = 0; age < 8; age++) {
            flying.update(age);
        }
        Color nearer = SimulationRenderer.colorFor(flying, world);
        float[] farHsb = Color.RGBtoHSB(far.getRed(), far.getGreen(), far.getBlue(), null);
        float[] nearHsb = Color.RGBtoHSB(nearer.getRed(), nearer.getGreen(), nearer.getBlue(), null);
        assertEquals(true, nearHsb[0] < farHsb[0], "closer rockets are warmer: " + nearHsb[0] + " vs " + farHsb[0]);

        for (int age = 8; age < 40; age++) {
            flying.update(age);
        }
        assertEquals(SimulationRenderer.ON_TARGET, SimulationRenderer.colorFor(flying, world));

        Rocket crashed = new Rocket(new DNA(right, rate, random), world);
        for (int age = 0; age < 40; age++) {
            crashed.update(age);
        }
        assertEquals(SimulationRenderer.CRASHED, SimulationRenderer.colorFor(crashed, world));
    }

    @Test
    void thumbnailKeepsAspectRatioAndShowsTheCourse() {
        World world = CourseLayout.CLASSIC.create(1024, 768);
        BufferedImage thumb = SimulationRenderer.thumbnail(world, 160, 160);
        assertEquals(160, thumb.getWidth());
        assertEquals(120, thumb.getHeight());
        // The first barrier spans the left 7/8 at two thirds height; sample well inside it.
        assertEquals(SimulationRenderer.BARRIER.getRGB(), thumb.getRGB(40, 81));
        assertEquals(SimulationRenderer.BACKGROUND.getRGB(), thumb.getRGB(150, 60));
    }
}
