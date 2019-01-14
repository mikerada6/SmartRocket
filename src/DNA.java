import java.awt.*;
import java.util.Random;

public class DNA {

    private int lifespan = 400;
    private Vector[] genes;
    private int R;
    private int G;
    private int B;

    public DNA() {
        genes = new Vector[lifespan];
        Random rand = new Random();
        for (int i = 0; i < genes.length; i++) {
            Vector temp = new Vector(rand.nextInt(), rand.nextInt());
            genes[i] = temp.setMag(Rocket.maxvVelocity);
        }
        R = rand.nextInt(255);
        G = rand.nextInt(255);
        B = rand.nextInt(255);
    }

    public Vector getGene(int i)
    {
        return genes[i];
    }
    public DNA(Vector[] genes) {
        this.genes = genes;
    }

    public Color getColor()
    {
        return new Color(R,G,B);
    }
}
