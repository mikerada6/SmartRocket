package io.github.mikerada6.smartrocket.report;

import io.github.mikerada6.smartrocket.simulation.GenerationStats;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

/** Per-generation statistics of the current run, in order, for charting. No Swing. */
public final class StatsHistory {

    private final List<GenerationStats> generations = new ArrayList<>();

    public void add(GenerationStats stats) {
        generations.add(stats);
    }

    public void clear() {
        generations.clear();
    }

    public int size() {
        return generations.size();
    }

    public boolean isEmpty() {
        return generations.isEmpty();
    }

    public GenerationStats latest() {
        return generations.get(generations.size() - 1);
    }

    public List<GenerationStats> all() {
        return List.copyOf(generations);
    }

    /** Largest value of a series across all generations, or 0 when empty. */
    public double max(ToDoubleFunction<GenerationStats> series) {
        double max = 0;
        for (GenerationStats g : generations) {
            max = Math.max(max, series.applyAsDouble(g));
        }
        return max;
    }

    /** Generation index of the first hit, or -1 if no generation has had one. */
    public int firstGenerationWithHit() {
        for (GenerationStats g : generations) {
            if (g.hitRockets() > 0) {
                return g.generation();
            }
        }
        return -1;
    }
}
