package io.github.mikerada6.smartrocket;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/** Draws a {@link Simulation} onto any Graphics2D. The only class that knows how things look. */
public final class SimulationRenderer {

    private static final Color BACKGROUND = Color.BLACK;
    private static final Color BARRIER = Color.RED;
    private static final Color TARGET = Color.GREEN;
    private static final Color HUD = Color.WHITE;

    public void render(Graphics2D g, Simulation simulation, double fps) {
        World world = simulation.world();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, world.width(), world.height());

        g.setColor(BARRIER);
        for (Barrier b : world.barriers()) {
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        }

        for (Rocket rocket : simulation.population().getRockets()) {
            drawRocket(g, rocket);
        }

        Target target = world.target();
        int diameter = (int) Math.round(2 * target.radius());
        g.setColor(TARGET);
        g.fillOval((int) Math.round(target.centre().x() - target.radius()),
                (int) Math.round(target.centre().y() - target.radius()), diameter, diameter);

        drawHud(g, simulation, fps);
    }

    private static void drawRocket(Graphics2D g, Rocket rocket) {
        Vec2 pos = rocket.position();
        double cx = pos.x() + Rocket.WIDTH / 2.0;
        double cy = pos.y() + Rocket.HEIGHT / 2.0;
        // The sprite is drawn pointing up; rotate it to face the direction of travel.
        double rotation = rocket.heading() + Math.PI / 2;
        AffineTransform old = g.getTransform();
        g.rotate(rotation, cx, cy);
        g.setColor(rocket.color());
        g.fillRect((int) pos.x(), (int) pos.y(), Rocket.WIDTH, Rocket.HEIGHT);
        g.setTransform(old);
    }

    private static void drawHud(Graphics2D g, Simulation simulation, double fps) {
        g.setColor(HUD);
        int y = 20;
        g.drawString("Generation: " + simulation.generation(), 20, y);
        g.drawString("Age: " + simulation.age(), 20, y += 20);
        g.drawString(String.format("FPS: %.1f", fps), 20, y += 20);
        g.drawString("On target: " + simulation.hitsThisFrame(), 20, y += 20);
        GenerationStats last = simulation.lastGeneration();
        if (last != null) {
            g.drawString(String.format("Last gen: avg %.1f  max %.1f  hit %d  crashed %d",
                    last.averageFitness(), last.maxFitness(), last.hitRockets(), last.crashedRockets()), 20, y += 20);
        }
    }
}
