package io.github.mikerada6.smartrocket;

/**
 * Tunable parameters of a simulation run. Values are validated once here so the
 * simulation classes can assume they are sane.
 *
 * @param mutationRate probability, per gene, of replacement by a random gene when breeding
 * @param maxSpeed     upper bound on a rocket's speed in pixels per frame; infinity for none
 * @param elites       number of best rockets copied unchanged into the next generation
 */
public record SimulationConfig(int width, int height, int populationSize, int lifespan,
                               double mutationRate, double maxSpeed, int elites) {

    public static final double DEFAULT_MUTATION_RATE = 0.01;
    public static final double UNLIMITED_SPEED = Double.POSITIVE_INFINITY;

    public SimulationConfig {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("world size must be positive: " + width + "x" + height);
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("populationSize must be positive: " + populationSize);
        }
        if (lifespan <= 0) {
            throw new IllegalArgumentException("lifespan must be positive: " + lifespan);
        }
        if (mutationRate < 0 || mutationRate > 1 || Double.isNaN(mutationRate)) {
            throw new IllegalArgumentException("mutationRate must be between 0 and 1: " + mutationRate);
        }
        if (!(maxSpeed > 0)) {
            throw new IllegalArgumentException("maxSpeed must be positive: " + maxSpeed);
        }
        if (elites < 0 || elites > populationSize) {
            throw new IllegalArgumentException("elites must be between 0 and populationSize: " + elites);
        }
    }

    public SimulationConfig(int width, int height, int populationSize, int lifespan) {
        this(width, height, populationSize, lifespan, DEFAULT_MUTATION_RATE, UNLIMITED_SPEED, 0);
    }

    public SimulationConfig(int width, int height, int populationSize, int lifespan, double mutationRate) {
        this(width, height, populationSize, lifespan, mutationRate, UNLIMITED_SPEED, 0);
    }

    /** The values the original hard-coded simulation used. */
    public static SimulationConfig defaults() {
        return new SimulationConfig(1024, 768, 10_000, 200, DEFAULT_MUTATION_RATE, UNLIMITED_SPEED, 0);
    }
}
