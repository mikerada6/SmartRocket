package io.github.mikerada6.smartrocket;

import java.util.Random;

/**
 * Drives one population through successive generations. Holds all mutable simulation
 * state and knows nothing about rendering, so it can be stepped headlessly in tests.
 * Every random decision goes through the single {@link Random} passed in, so a run is
 * fully reproducible from its seed.
 */
public final class Simulation {

    private final SimulationConfig config;
    private final World world;
    private final Population population;
    private int age;
    private int generation;
    private int hitsThisFrame;
    private double lastAverageFitness;

    public Simulation(SimulationConfig config, World world, Random random) {
        this.config = config;
        this.world = world;
        this.population = new Population(config.populationSize(), config.lifespan(), world, random);
    }

    /** Advances every rocket by one frame and, at the end of a lifespan, breeds the next generation. */
    public void step() {
        hitsThisFrame = population.update(age);
        population.checkBarriers();
        age++;
        if (age >= config.lifespan()) {
            lastAverageFitness = population.evaluate();
            population.selection();
            age = 0;
            generation++;
        }
    }

    public SimulationConfig config() {
        return config;
    }

    public World world() {
        return world;
    }

    public Population population() {
        return population;
    }

    /** Frames elapsed in the current generation, from 0 to lifespan - 1. */
    public int age() {
        return age;
    }

    public int generation() {
        return generation;
    }

    /** Number of rockets sitting on the target after the most recent step. */
    public int hitsThisFrame() {
        return hitsThisFrame;
    }

    /** Average fitness of the most recently completed generation, or 0 before the first completes. */
    public double lastAverageFitness() {
        return lastAverageFitness;
    }
}
