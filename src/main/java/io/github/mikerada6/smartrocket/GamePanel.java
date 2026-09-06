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

    private transient Simulation simulation;
    private final transient SimulationRenderer renderer = new SimulationRenderer();
    private final Timer timer;
    private boolean paused;
    private int stepsPerFrame = 1;
    private int framesSinceSample;
    private long sampleStartNanos;
    private double measuredFps;

    public GamePanel(Simulation simulation) {
        setSimulation(simulation);
        setBackground(SimulationRenderer.BACKGROUND);
        timer = new Timer(1000 / FPS, e -> tick());
        timer.setCoalesce(true);
    }

    /** Shows a different run; the panel resizes to its world. */
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        World world = simulation.world();
        setPreferredSize(new Dimension(world.width(), world.height()));
        revalidate();
        repaint();
    }

    public Simulation simulation() {
        return simulation;
    }

    /** While paused the panel keeps painting but stops stepping. */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    /** Simulation steps per drawn frame; more than one fast-forwards. */
    public void setStepsPerFrame(int steps) {
        if (steps < 1) {
            throw new IllegalArgumentException("stepsPerFrame must be at least 1: " + steps);
        }
        this.stepsPerFrame = steps;
    }

    public int stepsPerFrame() {
        return stepsPerFrame;
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

    /** One timer tick: advance unless paused, then repaint. Package-private so tests can drive it. */
    void tick() {
        if (!paused) {
            for (int i = 0; i < stepsPerFrame; i++) {
                simulation.step();
            }
        }
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
