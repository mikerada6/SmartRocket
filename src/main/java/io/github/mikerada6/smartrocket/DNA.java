package io.github.mikerada6.smartrocket;

import java.awt.Color;
import java.util.Random;

/** A rocket's genome: one thrust vector per frame of life, plus a display colour. */
public final class DNA {

    /** Probability that any single gene is replaced by a fresh random one during mutation. */
    private static final double MUTATION_RATE = 0.01;

    private final Random random;
    private final Vec2[] genes;
    private final Color color;

    /** A fully random genome of {@code lifespan} thrust vectors and a random colour. */
    public DNA(int lifespan, Random random) {
        this.random = random;
        genes = new Vec2[lifespan];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = randomGene();
        }
        color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public DNA(Vec2[] genes, Color color, Random random) {
        this.random = random;
        this.genes = genes.clone();
        this.color = color;
    }

    /** A thrust vector of fixed magnitude in a uniformly random direction. */
    private Vec2 randomGene() {
        return Vec2.fromAngle(random.nextDouble() * 2 * Math.PI, Rocket.MAX_THRUST);
    }

    /** Number of genes, which is also the number of frames a rocket lives. */
    public int length() {
        return genes.length;
    }

    public Vec2 getGene(int i) {
        return genes[i % genes.length];
    }

    public Color getColor() {
        return color;
    }

    /** Child taking genes up to a random point from {@code partner} and the rest from this genome. */
    public DNA crossover(DNA partner) {
        int mid = random.nextInt(genes.length);
        Vec2[] newgenes = new Vec2[genes.length];
        for (int i = 0; i < genes.length; i++) {
            newgenes[i] = i > mid ? genes[i] : partner.genes[i];
        }
        return new DNA(newgenes, blend(color, partner.color), random);
    }

    /** Root-mean-square blend of two colours, so mixing never darkens toward black. */
    private static Color blend(Color a, Color b) {
        return new Color(rms(a.getRed(), b.getRed()), rms(a.getGreen(), b.getGreen()), rms(a.getBlue(), b.getBlue()));
    }

    private static int rms(int a, int b) {
        return (int) Math.sqrt((a * a + b * b) / 2.0);
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
        for (Vec2 gene : genes) {
            ans.append(gene).append('\n');
        }
        return ans.toString();
    }
}
