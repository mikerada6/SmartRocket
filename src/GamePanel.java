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

    public static final int FPS = 45;
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    public static int totalFrameCount;
    public boolean running;
    private BufferedImage image;
    private Graphics2D g;
    private double averageFPS;
    private Thread thread;
    private Population p;
    public static final Target t= new Target();
    public static int age;
    public static int generation;
    public static double stat;
    public static double barrierx;
    public static double barriery;
    public static double barrierw;
    public static double barrierh;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        BufferedImage img = null;
        totalFrameCount = 0;
        p = new Population();
        generation=0;
        age=0;
        barrierw = WIDTH / 2;
        barrierh = 10;
        barrierx = (WIDTH - barrierw) / 2;
        barriery = (HEIGHT - barrierh) / 2;

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
        p.update();
        age++;
        if (age >= DNA.lifespan) {
            stat = p.evaluate();
            p.selection();
            age = 0;
            generation++;
        }
    }

    public void gameRender() {
        //draw the FPS onto the screen
        g.setColor(Color.RED);
        g.fillRect((int)(barrierx), (int)(barriery), (int)barrierw, (int)barrierh);
        g.setColor(Color.WHITE);
        p.draw(g);
        t.draw(g);
        g.setColor(Color.WHITE);
        g.drawString("Generation: " + generation, 20, 20);
        g.drawString("Age: " + age, 20, 40);
        if (stat != 0) {
            g.drawString("Stat: " + stat, 20, 60);
            g.drawString("FPS: " + averageFPS, 20, 80);
        }
        else
        {
            g.drawString("FPS: " + averageFPS, 20, 60);
        }


    }

    public void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

    }

    public void keyTyped(KeyEvent key) {

    }


    public void keyReleased(KeyEvent key) {
    }

    public static double map(double num, double minInput, double maxInput, double minOutput, double maxOutput)
    {
        double slope = (maxOutput-minOutput)/(maxInput-minOutput);
        double b = maxOutput-minOutput*slope;
        return slope * num + b;
    }

}
