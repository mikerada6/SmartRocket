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
    private final transient SimulationRenderer renderer = new SimulationRenderer();
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
        renderer.render((Graphics2D) g, simulation, measuredFps);
    }
}
