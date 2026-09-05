package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerationLogTest {

    @Test
    void writesHeaderThenOneLinePerGeneration(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("generations.csv");
        try (GenerationLog log = new GenerationLog(file)) {
            log.accept(new GenerationStats(0, 26.5, 59.7, 0, 10000, -1));
            log.accept(new GenerationStats(1, 30.0, 8000.0, 3, 9990, 150));
        }
        List<String> lines = Files.readAllLines(file);
        assertEquals(List.of(
                GenerationStats.CSV_HEADER,
                "0,26.5,59.7,0,10000,-1",
                "1,30.0,8000.0,3,9990,150"), lines);
    }

    @Test
    void eachLineIsVisibleBeforeClose(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("generations.csv");
        try (GenerationLog log = new GenerationLog(file)) {
            log.accept(new GenerationStats(0, 1, 2, 0, 0, -1));
            assertEquals(2, Files.readAllLines(file).size());
        }
    }
}
