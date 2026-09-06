package io.github.mikerada6.smartrocket;

import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Drives one population through successive generations. Holds all mutable simulation
 * state and knows nothing about rendering, so it can be stepped headlessly in tests.
 * Every random decision goes through the single {@link Random} passed in, so a run is
 * fully reproducible from its seed.
 */
public final class Simulation {

    private static final Logger LOG = Logger.getLogger(Simulation.class.getName());

    private final SimulationConfig config;
    private final World world;
    private final Population population;
    private final Consumer<GenerationStats> onGenerationComplete;
    private int age;
    private int generation;
    private int hitsThisFrame;
    private int firstHitAge = -1;
    private GenerationStats lastGeneration;

    public Simulation(SimulationConfig config, World world, Random random) {
        this(config, world, random, stats -> { });
    }

    /**
     * @param onGenerationComplete called once per generation with its statistics, before
     *                             the next generation is bred
     */
    public Simulation(SimulationConfig config, World world, Random random,
                      Consumer<GenerationStats> onGenerationComplete) {
        this.config = config;
        this.world = world;
        this.population = new Population(config.populationSize(), config.lifespan(), world, random);
        this.onGenerationComplete = onGenerationComplete;
        LOG.info(() -> "simulation created: " + config + ", " + world.barriers().size() + " barriers");
    }

    /** Advances every rocket by one frame and, at the end of a lifespan, breeds the next generation. */
    public void step() {
        hitsThisFrame = population.update(age);
        if (hitsThisFrame > 0 && firstHitAge < 0) {
            firstHitAge = age;
        }
        population.checkBarriers();
        age++;
        if (age >= config.lifespan()) {
            completeGeneration();
        }
    }

    private void completeGeneration() {
        double average = population.evaluate();
        lastGeneration = new GenerationStats(generation, average, population.maxFitness(),
                population.hitCount(), population.crashedCount(), firstHitAge);
        LOG.info(lastGeneration::toString);
        onGenerationComplete.accept(lastGeneration);
        population.selection();
        age = 0;
        firstHitAge = -1;
        generation++;
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

    /** Statistics of the most recently completed generation, or null before the first completes. */
    public GenerationStats lastGeneration() {
        return lastGeneration;
    }

    /** Average fitness of the most recently completed generation, or 0 before the first completes. */
    public double lastAverageFitness() {
        return lastGeneration == null ? 0 : lastGeneration.averageFitness();
    }
}
