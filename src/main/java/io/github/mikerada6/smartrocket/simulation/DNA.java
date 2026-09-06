package io.github.mikerada6.smartrocket.simulation;

import io.github.mikerada6.smartrocket.geometry.Vec2;

import java.util.Random;

/** A rocket's genome: one thrust vector per frame of life. */
public final class DNA {

    private final Random random;
    /** Probability that any single gene is replaced by a fresh random one during mutation. */
    private final double mutationRate;
    private final Vec2[] genes;

    /** A fully random genome of {@code lifespan} thrust vectors. */
    public DNA(int lifespan, double mutationRate, Random random) {
        this.random = random;
        this.mutationRate = mutationRate;
        genes = new Vec2[lifespan];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = randomGene();
        }
    }

    public DNA(Vec2[] genes, double mutationRate, Random random) {
        this.random = random;
        this.mutationRate = mutationRate;
        this.genes = genes.clone();
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

    /** Child taking genes up to a random point from {@code partner} and the rest from this genome. */
    public DNA crossover(DNA partner) {
        int mid = random.nextInt(genes.length);
        Vec2[] newgenes = new Vec2[genes.length];
        for (int i = 0; i < genes.length; i++) {
            newgenes[i] = i > mid ? genes[i] : partner.genes[i];
        }
        return new DNA(newgenes, mutationRate, random);
    }

    public void mutation() {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
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
