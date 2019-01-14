import java.util.Random;

public class DNA {

    private int lifespan = 400;
    private Vector[] genes;

    public DNA() {
        genes = new Vector[lifespan];
        Random rand = new Random();
        for (int i = 0; i < genes.length; i++) {
            Vector temp = new Vector(rand.nextInt(), rand.nextInt());
            genes[i] = temp.setMag(Rocket.maxvVelocity);
        }
    }

    public Vector getGene(int i)
    {
        return genes[i];
    }
    public DNA(Vector[] genes) {
        this.genes = genes;
    }
}
