package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartRocketsTest {

    @Test
    void headlessRunCompletesTheRequestedGenerationsAndLogsEach(@TempDir Path dir) throws IOException {
        Path csv = dir.resolve("run.csv");
        Arguments arguments = Arguments.parse(new String[]{
                "--headless", "3", "--population", "20", "--lifespan", "10", "--seed", "1", "--log", csv.toString()});

        GenerationStats last = SmartRockets.runHeadless(arguments);

        assertEquals(2, last.generation());
        assertEquals(4, Files.readAllLines(csv).size(), "header plus one line per generation");
    }

    @Test
    void headlessRunsWithTheSameSeedAreIdentical(@TempDir Path dir) throws IOException {
        String[] args = {"--headless", "2", "--population", "30", "--lifespan", "10", "--seed", "9",
                "--course", "CLASSIC", "--log", dir.resolve("a.csv").toString()};
        GenerationStats a = SmartRockets.runHeadless(Arguments.parse(args));
        args[args.length - 1] = dir.resolve("b.csv").toString();
        GenerationStats b = SmartRockets.runHeadless(Arguments.parse(args));
        assertEquals(a, b);
    }

    @Test
    void headlessRunAcceptsACourseFile(@TempDir Path dir) throws IOException {
        Path course = dir.resolve("open.course");
        Files.writeString(course, "size 300 300\ntarget 150 40 30\n");
        Arguments arguments = Arguments.parse(new String[]{
                "--headless", "2", "--population", "50", "--lifespan", "20", "--seed", "3",
                "--course-file", course.toString(), "--log", dir.resolve("run.csv").toString()});

        GenerationStats last = SmartRockets.runHeadless(arguments);

        assertEquals(1, last.generation());
    }
}
