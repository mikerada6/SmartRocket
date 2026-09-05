package io.github.mikerada6.smartrocket;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.List;

/** Draws a {@link Simulation} onto any Graphics2D. The only class that knows how things look. */
public final class SimulationRenderer {

    public static final Color BACKGROUND = new Color(24, 24, 28);
    public static final Color BARRIER = new Color(192, 57, 43);
    public static final Color TARGET = new Color(46, 204, 113);
    public static final Color HUD = new Color(230, 230, 230);
    public static final Color ON_TARGET = Color.WHITE;
    public static final Color CRASHED = new Color(80, 80, 88);
    public static final Color ELITE_OUTLINE = new Color(255, 255, 255, 200);
    public static final Color TRAIL = new Color(255, 255, 255, 70);
    public static final Color LAUNCH = new Color(230, 230, 230, 160);

    /** Hue for a rocket as far from the target as possible (blue) and for one touching it (orange). */
    private static final float FAR_HUE = 0.62f;
    private static final float NEAR_HUE = 0.08f;

    public void render(Graphics2D g, Simulation simulation, double fps) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        World world = simulation.world();
        drawBackgroundAndBarriers(g, world);
        drawLaunch(g, world.launch());
        List<Rocket> rockets = simulation.population().getRockets();
        for (Rocket rocket : rockets) {
            if (!rocket.isElite()) {
                drawRocket(g, rocket, world);
            }
        }
        // Elites last so their trails and outlines sit on top of the crowd.
        for (Rocket rocket : rockets) {
            if (rocket.isElite()) {
                drawTrail(g, rocket);
                drawRocket(g, rocket, world);
            }
        }
        drawTarget(g, world.target());
        drawHud(g, simulation, fps);
    }

    /** Everything static in a world, without rockets; shared with the course editor. */
    public void drawWorld(Graphics2D g, World world) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackgroundAndBarriers(g, world);
        drawLaunch(g, world.launch());
        drawTarget(g, world.target());
    }

    /** Outline of a rocket at the launch point with a chevron above it, so the start is visible when empty. */
    private static void drawLaunch(Graphics2D g, Vec2 launch) {
        int x = (int) Math.round(launch.x());
        int y = (int) Math.round(launch.y());
        g.setColor(LAUNCH);
        g.drawRect(x, y, Rocket.WIDTH, Rocket.HEIGHT);
        int cx = x + Rocket.WIDTH / 2;
        g.drawLine(cx - 5, y - 4, cx, y - 9);
        g.drawLine(cx, y - 9, cx + 5, y - 4);
    }

    /**
     * Colour encoding a rocket's state: white once on the target, dim grey after a crash,
     * otherwise a hue from blue (far from the target) to orange (about to reach it).
     */
    public static Color colorFor(Rocket rocket, World world) {
        if (rocket.hasHitTarget()) {
            return ON_TARGET;
        }
        if (rocket.hasCrashed()) {
            return CRASHED;
        }
        double diagonal = Math.hypot(world.width(), world.height());
        double nearness = Math.max(0, Math.min(1, 1 - rocket.distanceToTarget() / diagonal));
        return Color.getHSBColor(FAR_HUE + (NEAR_HUE - FAR_HUE) * (float) nearness, 0.85f, 1f);
    }

    private static void drawBackgroundAndBarriers(Graphics2D g, World world) {
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, world.width(), world.height());
        g.setColor(BARRIER);
        for (Barrier b : world.barriers()) {
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        }
    }

    private static void drawTarget(Graphics2D g, Target target) {
        int diameter = (int) Math.round(2 * target.radius());
        g.setColor(TARGET);
        g.fillOval((int) Math.round(target.centre().x() - target.radius()),
                (int) Math.round(target.centre().y() - target.radius()), diameter, diameter);
    }

    private static void drawRocket(Graphics2D g, Rocket rocket, World world) {
        Vec2 pos = rocket.position();
        double cx = pos.x() + Rocket.WIDTH / 2.0;
        double cy = pos.y() + Rocket.HEIGHT / 2.0;
        // The sprite is drawn pointing up; rotate it to face the direction of travel.
        double rotation = rocket.heading() + Math.PI / 2;
        AffineTransform old = g.getTransform();
        g.rotate(rotation, cx, cy);
        g.setColor(colorFor(rocket, world));
        g.fillRect((int) pos.x(), (int) pos.y(), Rocket.WIDTH, Rocket.HEIGHT);
        if (rocket.isElite()) {
            g.setColor(ELITE_OUTLINE);
            g.drawRect((int) pos.x() - 1, (int) pos.y() - 1, Rocket.WIDTH + 1, Rocket.HEIGHT + 1);
        }
        g.setTransform(old);
    }

    private static void drawTrail(Graphics2D g, Rocket rocket) {
        List<Vec2> trail = rocket.trail();
        if (trail.size() < 2) {
            return;
        }
        var oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(TRAIL);
        for (int i = 1; i < trail.size(); i++) {
            Vec2 a = trail.get(i - 1);
            Vec2 b = trail.get(i);
            g.drawLine((int) (a.x() + Rocket.WIDTH / 2.0), (int) (a.y() + Rocket.HEIGHT / 2.0),
                    (int) (b.x() + Rocket.WIDTH / 2.0), (int) (b.y() + Rocket.HEIGHT / 2.0));
        }
        g.setStroke(oldStroke);
    }

    private static void drawHud(Graphics2D g, Simulation simulation, double fps) {
        g.setColor(HUD);
        int y = 20;
        g.drawString("Generation " + simulation.generation() + "   age " + simulation.age()
                + "/" + simulation.config().lifespan(), 20, y);
        g.drawString(String.format("%.0f fps   %d on target", fps, simulation.hitsThisFrame()), 20, y += 18);
    }
}
