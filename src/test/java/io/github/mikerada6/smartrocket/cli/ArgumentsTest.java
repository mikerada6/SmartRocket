package io.github.mikerada6.smartrocket.cli;

import io.github.mikerada6.smartrocket.simulation.SimulationConfig;
import io.github.mikerada6.smartrocket.world.CourseLayout;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgumentsTest {

    @Test
    void noArgumentsGivesDefaults() {
        Arguments a = Arguments.parse(new String[0]);
        assertEquals(SimulationConfig.defaults(), a.config());
        assertEquals(CourseLayout.EASY, a.course());
        assertTrue(a.courseFile().isEmpty());
        assertTrue(a.seed().isEmpty());
        assertFalse(a.headless());
        assertEquals(Arguments.DEFAULT_LOG_PATH, a.logPath());
        assertFalse(a.edit());
        assertFalse(a.help());
    }

    @Test
    void everyOptionIsParsed() {
        Arguments a = Arguments.parse(new String[]{
                "--population", "500", "--lifespan", "50", "--width", "640", "--height", "480",
                "--mutation-rate", "0.05", "--max-speed", "8", "--elite-fraction", "0.02",
                "--course", "classic", "--seed", "7", "--headless", "3", "--log", "out/run.csv"});
        assertEquals(new SimulationConfig(640, 480, 500, 50, 0.05, 8, 0.02), a.config());
        assertEquals(10, a.config().elites());
        assertEquals(CourseLayout.CLASSIC, a.course());
        assertEquals(7, a.seed().getAsLong());
        assertTrue(a.headless());
        assertEquals(3, a.headlessGenerations());
        assertEquals(Path.of("out/run.csv"), a.logPath());
    }

    @Test
    void helpFlagIsRecognisedInBothForms() {
        assertTrue(Arguments.parse(new String[]{"--help"}).help());
        assertTrue(Arguments.parse(new String[]{"-h"}).help());
    }

    @Test
    void usageMentionsEveryOption() {
        for (String flag : new String[]{"--population", "--lifespan", "--width", "--height",
                "--mutation-rate", "--max-speed", "--elite-fraction", "--course", "--course-file", "--seed", "--headless", "--edit", "--log", "--help"}) {
            assertTrue(Arguments.USAGE.contains(flag), "usage lacks " + flag);
        }
    }

    @Test
    void badInputIsRejectedWithTheOffendingFlagNamed() {
        assertMessageContains("--population", () -> Arguments.parse(new String[]{"--population", "ten"}));
        assertMessageContains("--population", () -> Arguments.parse(new String[]{"--population"}));
        assertMessageContains("--course", () -> Arguments.parse(new String[]{"--course", "HARD"}));
        assertMessageContains("--headless", () -> Arguments.parse(new String[]{"--headless", "-1"}));
        assertMessageContains("--bogus", () -> Arguments.parse(new String[]{"--bogus", "1"}));
        assertMessageContains("stray", () -> Arguments.parse(new String[]{"stray"}));
        assertMessageContains("mutationRate", () -> Arguments.parse(new String[]{"--mutation-rate", "1.5"}));
        assertMessageContains("maxSpeed", () -> Arguments.parse(new String[]{"--max-speed", "0"}));
        assertMessageContains("eliteFraction", () -> Arguments.parse(new String[]{"--elite-fraction", "1.5"}));
    }

    @Test
    void seededRandomsAreReproducible() {
        Arguments a = Arguments.parse(new String[]{"--seed", "3"});
        assertEquals(a.newRandom().nextLong(), a.newRandom().nextLong());
    }

    private static void assertMessageContains(String expected, org.junit.jupiter.api.function.Executable call) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, call);
        assertTrue(e.getMessage().contains(expected), "message '" + e.getMessage() + "' lacks '" + expected + "'");
    }

    @Test
    void courseFileIsParsedAndExcludesExplicitSize() throws IOException {
        Arguments a = Arguments.parse(new String[]{"--course-file", "courses/classic.course"});
        assertEquals(Path.of("courses/classic.course"), a.courseFile().orElseThrow());
        assertEquals(CourseLayout.CLASSIC.create(1024, 768), a.loadWorld());

        assertMessageContains("--course-file", () -> Arguments.parse(new String[]{"--course-file", "x", "--width", "10"}));
        assertMessageContains("--course-file", () -> Arguments.parse(new String[]{"--height", "10", "--course-file", "x"}));
    }

    @Test
    void withoutACourseFileTheBuiltInCourseIsScaledToTheConfiguredSize() throws IOException {
        Arguments a = Arguments.parse(new String[]{"--course", "EASY", "--width", "500", "--height", "400"});
        assertEquals(CourseLayout.EASY.create(500, 400), a.loadWorld());
    }

    @Test
    void editModeIsParsedAndCannotBeHeadless(@org.junit.jupiter.api.io.TempDir Path dir) throws IOException {
        Arguments a = Arguments.parse(new String[]{"--edit", "--course-file", dir.resolve("new.course").toString()});
        assertTrue(a.edit());
        assertEquals(CourseLayout.EASY.create(1024, 768), a.loadWorldForEditing(),
                "a missing file starts from the built-in course");
        assertMessageContains("--edit", () -> Arguments.parse(new String[]{"--edit", "--headless", "2"}));
    }
}
