package io.github.mikerada6.smartrocket;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Population {

    private final int size;
    private final int lifespan;
    private final World world;
    private final Random random;
    private Rocket[] rockets;
    private List<Rocket> matingPool;

    public Population(int size, int lifespan, World world, Random random) {
        this.size = size;
        this.lifespan = lifespan;
        this.world = world;
        this.random = random;
        rockets = new Rocket[size];
        matingPool = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            rockets[i] = new Rocket(new DNA(lifespan, random), world);
        }
    }

    /**
     * Scores every rocket and rebuilds the mating pool in proportion to fitness.
     *
     * @return the population's average fitness
     */
    public double evaluate() {
        double avgFit = 0;
        double maxFit = 0;
        Arrays.sort(rockets);
        for (Rocket r : rockets) {
            double fit = r.calcFitness();
            if (maxFit < fit) {
                maxFit = fit;
            }
            avgFit += fit;
        }
        avgFit /= rockets.length;
        for (Rocket r : rockets) {
            r.setMatingEligibility(r.calcFitness() / maxFit);
        }

        matingPool = new ArrayList<>();
        for (Rocket r : rockets) {
            double n = r.getMatingEligibility() * 100;
            for (int j = 0; j < n; j++) {
                matingPool.add(r);
            }
        }
        return avgFit;
    }

    /** Replaces every rocket with a mutated child of two parents drawn from the mating pool. */
    public void selection() {
        Rocket[] newRockets = new Rocket[size];
        for (int i = 0; i < rockets.length; i++) {
            DNA parentA = random(matingPool).getDna();
            DNA parentB = random(matingPool).getDna();
            DNA child = parentA.crossover(parentB);
            child.mutation();
            newRockets[i] = new Rocket(child, world);
        }
        this.rockets = newRockets;
    }

    private Rocket random(List<Rocket> list) {
        return list.get(random.nextInt(list.size()));
    }

    public void checkBarriers() {
        for (Rocket r : rockets) {
            r.checkBarriers();
        }
    }

    /**
     * Advances every rocket one frame.
     *
     * @return how many rockets are on the target after this frame
     */
    public int update(int age) {
        int hits = 0;
        for (Rocket r : rockets) {
            if (r.update(age)) {
                hits++;
            }
        }
        this.evaluate();
        return hits;
    }

    public List<Rocket> getRockets() {
        return List.of(rockets);
    }

    public int getLifespan() {
        return lifespan;
    }

    public Graphics draw(Graphics g) {
        for (Rocket r : rockets) {
            r.draw(g);
        }
        return g;
    }
}
