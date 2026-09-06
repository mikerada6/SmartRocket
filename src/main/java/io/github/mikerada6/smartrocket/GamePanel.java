package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;

/** Swing view of a {@link Simulation}: steps it on a background thread and paints each frame. */
public class GamePanel extends JPanel implements Runnable {

    public static final int FPS = 60;

    private final Simulation simulation;
    private final int width;
    private final int height;
    private BufferedImage image;
    private Graphics2D g;
    private double averageFPS;
    private Thread thread;
    private volatile boolean running;

    public GamePanel(Simulation simulation) {
        this.simulation = simulation;
        this.width = simulation.world().width();
        this.height = simulation.world().height();
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        requestFocus();
        try {
            String str = "generation \ttotalFrameCount\t hit\n";
            BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt"));
            writer.write(str);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error: " + e);
            int error = 0 / 0;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        running = true;

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = FPS;

        long targetTime = 1000 / FPS;

        while (running) {
            startTime = System.nanoTime();
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g = (Graphics2D) image.getGraphics();
            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - URDTimeMillis;

            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {

            }
            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000.0);
                frameCount = 0;
                totalTime = 0;
            }
        }
    }

    public void gameUpdate() {
        int generation = simulation.generation();
        int age = simulation.age();
        simulation.step();
        String str = generation + "\t" + age + "\t" + simulation.hitsThisFrame() + "\n";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
            writer.append(' ');
            writer.append(str);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error: " + e);
            int error = 0 / 0;
        }
    }

    public void gameRender() {
        g.setColor(Color.RED);
        for (Barrier b : simulation.world().barriers()) {
            b.draw(g);
        }
        g.setColor(Color.WHITE);
        simulation.population().draw(g);
        simulation.world().target().draw(g);
        g.setColor(Color.WHITE);
        g.drawString("Generation: " + simulation.generation(), 20, 20);
        g.drawString("Age: " + simulation.age(), 20, 40);
        double stat = simulation.lastAverageFitness();
        if (stat != 0) {
            g.drawString("Stat: " + stat, 20, 60);
            g.drawString("FPS: " + averageFPS, 20, 80);
            g.drawString("Hit: " + simulation.hitsThisFrame(), 20, 100);
        } else {
            g.drawString("FPS: " + averageFPS, 20, 60);
            g.drawString("Hit: " + simulation.hitsThisFrame(), 20, 80);
        }
    }

    public void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }
}
