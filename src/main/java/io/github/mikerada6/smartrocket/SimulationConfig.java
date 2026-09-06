package io.github.mikerada6.smartrocket;

/**
 * Tunable parameters of a simulation run. Values are validated once here so the
 * simulation classes can assume they are sane.
 *
 * @param mutationRate probability, per gene, of replacement by a random gene when breeding
 * @param maxSpeed      upper bound on a rocket's speed in pixels per frame; infinity for none
 * @param eliteFraction share of the population, by fitness, re-flown unchanged each generation
 */
public record SimulationConfig(int width, int height, int populationSize, int lifespan,
                               double mutationRate, double maxSpeed, double eliteFraction) {

    public static final double DEFAULT_MUTATION_RATE = 0.01;
    public static final double UNLIMITED_SPEED = Double.POSITIVE_INFINITY;
    /**
     * Chosen from headless runs on the CLASSIC course: a cap of 14 solved it on every
     * seed tried, 12 only sometimes, 10 never, and 16 was comparable. See ADR 0005.
     */
    public static final double DEFAULT_MAX_SPEED = 14;
    /** One percent of the population; with the speed cap it roughly halves the generations to a first hit. */
    public static final double DEFAULT_ELITE_FRACTION = 0.01;

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
        if (eliteFraction < 0 || eliteFraction > 1 || Double.isNaN(eliteFraction)) {
            throw new IllegalArgumentException("eliteFraction must be between 0 and 1: " + eliteFraction);
        }
    }

    public SimulationConfig(int width, int height, int populationSize, int lifespan) {
        this(width, height, populationSize, lifespan, DEFAULT_MUTATION_RATE, DEFAULT_MAX_SPEED, DEFAULT_ELITE_FRACTION);
    }

    public SimulationConfig(int width, int height, int populationSize, int lifespan, double mutationRate) {
        this(width, height, populationSize, lifespan, mutationRate, DEFAULT_MAX_SPEED, DEFAULT_ELITE_FRACTION);
    }

    /** Number of rockets re-flown unchanged each generation, at least one whenever the fraction is positive. */
    public int elites() {
        if (eliteFraction == 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(eliteFraction * populationSize));
    }

    /** The original world and population size with the tuned algorithm settings. */
    public static SimulationConfig defaults() {
        return new SimulationConfig(1024, 768, 10_000, 200, DEFAULT_MUTATION_RATE, DEFAULT_MAX_SPEED, DEFAULT_ELITE_FRACTION);
    }
}
