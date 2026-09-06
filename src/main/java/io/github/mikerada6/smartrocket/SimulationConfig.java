package io.github.mikerada6.smartrocket;

/**
 * Tunable parameters of a simulation run. Values are validated once here so the
 * simulation classes can assume they are sane.
 *
 * @param mutationRate probability, per gene, of replacement by a random gene when breeding
 */
public record SimulationConfig(int width, int height, int populationSize, int lifespan, double mutationRate) {

    public static final double DEFAULT_MUTATION_RATE = 0.01;

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
    }

    public SimulationConfig(int width, int height, int populationSize, int lifespan) {
        this(width, height, populationSize, lifespan, DEFAULT_MUTATION_RATE);
    }

    /** The values the original hard-coded simulation used. */
    public static SimulationConfig defaults() {
        return new SimulationConfig(1024, 768, 10_000, 200, DEFAULT_MUTATION_RATE);
    }
}
