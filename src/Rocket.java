import java.awt.*;
import java.awt.geom.AffineTransform;


public class Rocket {

    public static final double maxvVelocity = 4;
    private Vector pos;
    private Vector vel;
    private Vector acc;
    private DNA dna;
    private int rocketHeight=25;
    private int rocketWidth=5;

    public Rocket() {
        dna = new DNA();
        pos = new Vector(GamePanel.WIDTH/2,GamePanel.HEIGHT-rocketHeight);
        vel=new Vector(0,0);
        acc = new Vector(0,0);
    }

    public void update() {
        applyForce(dna.getGene(GamePanel.totalFrameCount));
        vel = vel.add(acc);
        pos = pos.add(vel);
        acc = acc.multiply(0);
        //vel = vel.limit(maxvVelocity);
    }

    public void applyForce(Vector v) {
        this.acc = acc.add(v);
    }

    public Graphics draw(Graphics g) {
        AffineTransform transform = new AffineTransform();
        Graphics2D g2 =(Graphics2D)g;
        double theta = pos.getAngleRadians();

        transform.rotate(theta, this.pos.getX() + this.rocketWidth/2, this.pos.getY() + this.rocketHeight/2);
        AffineTransform old = g2.getTransform();
        g2.transform(transform);
        g2.fillRect((int) this.pos.getX(), (int) this.pos.getY(), rocketWidth, rocketHeight);
        g2.setTransform(old);

        return g;
    }

    public double getXPos()
    {
        return this.pos.getX();
    }

    public double getYPos()
    {
        return this.pos.getY();
    }


}
