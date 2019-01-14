import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Write a description of class GamePanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class GamePanel extends JPanel implements Runnable {

    public static final int FPS = 5;
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    public static int totalFrameCount;
    public boolean running;
    private BufferedImage image;
    private Graphics2D g;
    private double averageFPS;
    private Thread thread;
    private int populationSize;
    private Rocket[] rockets;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        BufferedImage img = null;
        totalFrameCount = 0;
        populationSize = 100;
        rockets = new Rocket[populationSize];


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

        for (int i = 0; i < rockets.length; i++) {
            rockets[i] = new Rocket();
        }

        while (running) {

            startTime = System.nanoTime();
            image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            g = (Graphics2D) image.getGraphics();
            gameUpdate();
            gameRender();
            gameDraw();
            totalFrameCount++;

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
        for (int i = 0; i < rockets.length; i++) {
            System.out.print(i + "\t" + totalFrameCount + "\t" + rockets[i].getXPos() + "\t" + rockets[i].getYPos());
            rockets[i].update();
            System.out.println("\t" + rockets[i].getXPos() + "\t" + rockets[i].getYPos());
        }
    }

    public void gameRender() {
        //draw the FPS onto the screen
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + averageFPS, 10, 10);
        for (Rocket r : rockets) {
            r.draw(g);
        }
    }

    public void gameDraw() {
        Graphics g2 = this.getGraphics();

        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        for (Rocket r : rockets) {
            r.draw(g);
        }
    }

    public void keyTyped(KeyEvent key) {

    }


    public void keyReleased(KeyEvent key) {
    }

}
