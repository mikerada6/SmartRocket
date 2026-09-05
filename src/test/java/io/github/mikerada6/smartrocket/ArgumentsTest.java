package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

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
        assertTrue(a.seed().isEmpty());
        assertFalse(a.headless());
        assertEquals(Arguments.DEFAULT_LOG_PATH, a.logPath());
        assertFalse(a.help());
    }

    @Test
    void everyOptionIsParsed() {
        Arguments a = Arguments.parse(new String[]{
                "--population", "500", "--lifespan", "50", "--width", "640", "--height", "480",
                "--mutation-rate", "0.05", "--max-speed", "8", "--elites", "10",
                "--course", "classic", "--seed", "7", "--headless", "3", "--log", "out/run.csv"});
        assertEquals(new SimulationConfig(640, 480, 500, 50, 0.05, 8, 10), a.config());
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
                "--mutation-rate", "--max-speed", "--elites", "--course", "--seed", "--headless", "--log", "--help"}) {
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
        assertMessageContains("elites", () -> Arguments.parse(new String[]{"--elites", "20", "--population", "10"}));
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
}
