package io.github.mikerada6.smartrocket;

/**
 * Tunable parameters of a simulation run. Values are validated once here so the
 * simulation classes can assume they are sane.
 */
public record SimulationConfig(int width, int height, int populationSize, int lifespan) {

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
    }

    /** The values the original hard-coded simulation used. */
    public static SimulationConfig defaults() {
        return new SimulationConfig(1024, 768, 10_000, 200);
    }
}
