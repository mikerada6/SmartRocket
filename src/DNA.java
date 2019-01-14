import java.awt.*;
import java.util.Random;

public class DNA {

    public static int lifespan = 200;
    Random random;
    private Vector[] genes;
    private int R;
    private int G;
    private int B;

    public DNA() {
        random = new Random();
        genes = new Vector[lifespan];
        for (int i = 0; i < genes.length; i++) {
            Vector temp = new Vector(random.nextInt(), random.nextInt());
            genes[i] = temp.setMag(Rocket.maxvVelocity);
        }
        R = random.nextInt(255);
        G = random.nextInt(255);
        B = random.nextInt(255);
    }

    public DNA(Vector[] genes, int R, int G, int B) {
        random = new Random();
        this.genes = genes;
        this.R = R;
        this.G = G;
        this.B = B;
    }



    public Vector getGene(int i) {
        return genes[i % lifespan];
    }

    public Color getColor() {
        return new Color(R, G, B);
    }

    public DNA crossover(DNA partner) {
        int mid = (int) random.nextInt(genes.length);
        Vector[] newgenes = new Vector[genes.length];
        for (int i = 0; i < genes.length; i++) {
            if (i > mid) {
                newgenes[i] = genes[i];
            } else {
                newgenes[i] = partner.genes[i];
            }
        }
        int newR = (int) Math.sqrt((this.R * this.R + partner.getR() * partner.getR()) / 2);
        int newG = (int) Math.sqrt((this.G * this.G + partner.getG() * partner.getG()) / 2);
        int newB = (int) Math.sqrt((this.B * this.B + partner.getB() * partner.getB()) / 2);
        return new DNA(newgenes, newR, newG, newB);

    }

    public int getR() {
        return R;
    }

    public int getG() {
        return G;
    }

    public int getB() {
        return B;
    }

    public void mutation() {

        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < 0.01) {
                Vector temp = new Vector(random.nextInt(), random.nextInt());
                genes[i] = temp.setMag(Rocket.maxvVelocity);
            }
        }
    }

    public String toString()
    {
        String ans ="";

        for(Vector g: genes)
        {
            ans+= g.toString()+"\n";
        }
        return ans;
    }
}
