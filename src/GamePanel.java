import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Write a description of class GamePanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class GamePanel extends JPanel implements Runnable {

    public static final int FPS = 30;
    public boolean running;
    public static final int WIDTH = 500;
    public static final int HEIGHT = 600;
    private BufferedImage image;
    private Graphics2D g;
    private double averageFPS;
    private Thread thread;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        BufferedImage img = null;

    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void run() {
        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = FPS;

        long targetTime = 1000 / FPS;

        while (running) {

            startTime = System.nanoTime();
            image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
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

    }

    public void gameRender(){


        //draw the FPS onto the screen
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + averageFPS, 10, 10);
    }

    public void gameDraw()
    {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    public void keyTyped(KeyEvent key) {

    }



    public void keyReleased(KeyEvent key) {
    }

}
