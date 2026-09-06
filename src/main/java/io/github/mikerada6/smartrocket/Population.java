package io.github.mikerada6.smartrocket;

import java.util.List;
import java.util.Random;

public class Population {

    private final int size;
    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private Rocket[] rockets;
    private WeightedPicker parentPicker;
    private double maxFitness;

    public Population(SimulationConfig config, World world, Random random) {
        this.size = config.populationSize();
        this.config = config;
        this.world = world;
        this.random = random;
        rockets = new Rocket[size];
        for (int i = 0; i < size; i++) {
            rockets[i] = new Rocket(new DNA(config.lifespan(), config.mutationRate(), random), world);
        }
    }

    /**
     * Scores every rocket once and prepares fitness-proportional parent selection.
     * Call at the end of a generation, not every frame: fitness only matters when breeding.
     *
     * @return the population's average fitness
     */
    public double evaluate() {
        double total = 0;
        maxFitness = Double.NEGATIVE_INFINITY;
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            double fit = rockets[i].evaluateFitness();
            total += fit;
            maxFitness = Math.max(maxFitness, fit);
            weights[i] = fit;
        }
        parentPicker = new WeightedPicker(weights, random);
        return total / size;
    }

    /** Replaces every rocket with a mutated child of two parents chosen in proportion to fitness. */
    public void selection() {
        if (parentPicker == null) {
            throw new IllegalStateException("evaluate() must run before selection()");
        }
        Rocket[] newRockets = new Rocket[size];
        for (int i = 0; i < size; i++) {
            DNA parentA = rockets[parentPicker.pick()].getDna();
            DNA parentB = rockets[parentPicker.pick()].getDna();
            DNA child = parentA.crossover(parentB);
            child.mutation();
            newRockets[i] = new Rocket(child, world);
        }
        this.rockets = newRockets;
        this.parentPicker = null;
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
        return hits;
    }

    /** Fitness of the best rocket as of the last {@link #evaluate()}. */
    public double maxFitness() {
        return maxFitness;
    }

    public int hitCount() {
        int n = 0;
        for (Rocket r : rockets) {
            if (r.hasHitTarget()) {
                n++;
            }
        }
        return n;
    }

    public int crashedCount() {
        int n = 0;
        for (Rocket r : rockets) {
            if (r.hasCrashed()) {
                n++;
            }
        }
        return n;
    }

    public List<Rocket> getRockets() {
        return List.of(rockets);
    }

    public int getLifespan() {
        return config.lifespan();
    }
}
