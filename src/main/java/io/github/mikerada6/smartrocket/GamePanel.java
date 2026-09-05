package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.*;

/**
 * Swing view of a {@link Simulation}. A {@link Timer} on the event dispatch thread steps
 * the simulation and requests a repaint at a fixed rate; all drawing happens in
 * {@link #paintComponent(Graphics)} on Swing's own double buffer.
 */
public final class GamePanel extends JPanel {

    public static final int FPS = 60;
    private static final long serialVersionUID = 1L;

    private final transient Simulation simulation;
    private final Timer timer;
    private int framesSinceSample;
    private long sampleStartNanos;
    private double measuredFps;

    public GamePanel(Simulation simulation) {
        this.simulation = simulation;
        World world = simulation.world();
        setPreferredSize(new Dimension(world.width(), world.height()));
        setBackground(Color.BLACK);
        timer = new Timer(1000 / FPS, e -> tick());
        timer.setCoalesce(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        sampleStartNanos = System.nanoTime();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private void tick() {
        simulation.step();
        framesSinceSample++;
        long now = System.nanoTime();
        long elapsed = now - sampleStartNanos;
        if (elapsed >= 1_000_000_000L) {
            measuredFps = framesSinceSample * 1e9 / elapsed;
            framesSinceSample = 0;
            sampleStartNanos = now;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        World world = simulation.world();
        g.setColor(Color.RED);
        for (Barrier b : world.barriers()) {
            b.draw(g);
        }
        simulation.population().draw(g);
        world.target().draw(g);
        drawHud(g);
    }

    private void drawHud(Graphics g) {
        g.setColor(Color.WHITE);
        int y = 20;
        g.drawString("Generation: " + simulation.generation(), 20, y);
        g.drawString("Age: " + simulation.age(), 20, y += 20);
        g.drawString(String.format("FPS: %.1f", measuredFps), 20, y += 20);
        g.drawString("On target: " + simulation.hitsThisFrame(), 20, y += 20);
        GenerationStats last = simulation.lastGeneration();
        if (last != null) {
            g.drawString(String.format("Last gen: avg %.1f  max %.1f  hit %d  crashed %d",
                    last.averageFitness(), last.maxFitness(), last.hitRockets(), last.crashedRockets()), 20, y += 20);
        }
    }
}
