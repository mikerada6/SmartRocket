package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsHistoryTest {

    @Test
    void tracksSeriesMaximaAndFirstHit() {
        StatsHistory history = new StatsHistory();
        assertTrue(history.isEmpty());
        assertEquals(0, history.max(GenerationStats::maxFitness));
        assertEquals(-1, history.firstGenerationWithHit());

        history.add(new GenerationStats(0, 30, 60, 0, 100, -1));
        history.add(new GenerationStats(1, 45, 9000, 3, 90, 150));
        history.add(new GenerationStats(2, 80, 8000, 20, 70, 120));

        assertEquals(3, history.size());
        assertEquals(9000, history.max(GenerationStats::maxFitness));
        assertEquals(100, history.max(GenerationStats::crashedRockets));
        assertEquals(1, history.firstGenerationWithHit());
        assertEquals(2, history.latest().generation());

        history.clear();
        assertTrue(history.isEmpty());
    }
}
