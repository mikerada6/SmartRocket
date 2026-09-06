package io.github.mikerada6.smartrocket;

/**
 * Summary of one completed generation, captured just before the next one is bred.
 *
 * @param generation     zero-based index of the generation that just finished
 * @param averageFitness mean fitness across the population
 * @param maxFitness     fitness of the best rocket
 * @param hitRockets     rockets that reached the target at any point in their life
 * @param crashedRockets rockets that left the world or struck a barrier
 * @param firstHitAge    age at which the first rocket reached the target, or -1 if none did
 */
public record GenerationStats(int generation, double averageFitness, double maxFitness,
                              int hitRockets, int crashedRockets, int firstHitAge) {

    public static final String CSV_HEADER = "generation,averageFitness,maxFitness,hitRockets,crashedRockets,firstHitAge";

    public String toCsv() {
        return generation + "," + averageFitness + "," + maxFitness + "," + hitRockets + ","
                + crashedRockets + "," + firstHitAge;
    }
}
