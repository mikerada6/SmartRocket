package io.github.mikerada6.smartrocket;

import java.awt.*;
import java.util.Random;

public class DNA {

    /** Probability that any single gene is replaced by a fresh random one during mutation. */
    private static final double MUTATION_RATE = 0.01;

    private final Random random;
    private final Vector[] genes;
    private final int r;
    private final int g;
    private final int b;

    /** A fully random genome of {@code lifespan} thrust vectors and a random colour. */
    public DNA(int lifespan, Random random) {
        this.random = random;
        genes = new Vector[lifespan];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = randomGene();
        }
        r = random.nextInt(255);
        g = random.nextInt(255);
        b = random.nextInt(255);
    }

    public DNA(Vector[] genes, int r, int g, int b, Random random) {
        this.random = random;
        this.genes = genes;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    private Vector randomGene() {
        Vector direction = new Vector(random.nextInt(), random.nextInt());
        return direction.setMag(Rocket.MAX_THRUST);
    }

    /** Number of genes, which is also the number of frames a rocket lives. */
    public int length() {
        return genes.length;
    }

    public Vector getGene(int i) {
        return genes[i % genes.length];
    }

    public Color getColor() {
        return new Color(r, g, b);
    }

    public DNA crossover(DNA partner) {
        int mid = random.nextInt(genes.length);
        Vector[] newgenes = new Vector[genes.length];
        for (int i = 0; i < genes.length; i++) {
            if (i > mid) {
                newgenes[i] = genes[i];
            } else {
                newgenes[i] = partner.genes[i];
            }
        }
        int newR = (int) Math.sqrt((this.r * this.r + partner.r * partner.r) / 2);
        int newG = (int) Math.sqrt((this.g * this.g + partner.g * partner.g) / 2);
        int newB = (int) Math.sqrt((this.b * this.b + partner.b * partner.b) / 2);
        return new DNA(newgenes, newR, newG, newB, random);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public void mutation() {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                genes[i] = randomGene();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Vector gene : genes) {
            ans.append(gene).append('\n');
        }
        return ans.toString();
    }
}
