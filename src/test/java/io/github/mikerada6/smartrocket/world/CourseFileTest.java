package io.github.mikerada6.smartrocket.world;

import io.github.mikerada6.smartrocket.geometry.Vec2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseFileTest {

    private static final World SAMPLE = new World(400, 300, new Target(new Vec2(200, 40.5), 12),
            List.of(new Barrier(0, 150, 300, 20), new Barrier(100, 60, 50, 10)));

    @Test
    void parsesStatementsCommentsAndBlankLines() {
        World world = CourseFile.parse("""
                # a course
                size 400 300

                target 200 40.5 12   # trailing comment
                barrier 0 150 300 20
                barrier 100 60 50 10
                """);
        assertEquals(SAMPLE, world);
    }

    @Test
    void formatThenParseRoundTrips() {
        assertEquals(SAMPLE, CourseFile.parse(CourseFile.format(SAMPLE)));
    }

    @Test
    void formatWritesWholeNumbersWithoutDecimals() {
        String text = CourseFile.format(SAMPLE);
        assertTrue(text.contains("target 200 40.5 12\n"), text);
        assertTrue(text.contains("size 400 300\n"), text);
        assertTrue(text.contains("launch 200 275\n"), text);
    }

    @Test
    void launchLineIsOptionalAndValidated() {
        World custom = CourseFile.parse("size 400 300\ntarget 200 40 12\nlaunch 20 30\n");
        assertEquals(new Vec2(20, 30), custom.launch());
        World defaulted = CourseFile.parse("size 400 300\ntarget 200 40 12\n");
        assertEquals(World.defaultLaunch(400, 300), defaulted.launch());
        assertMessage("launch point", () -> CourseFile.parse("size 400 300\ntarget 200 40 12\nlaunch 500 30\n"));
        assertMessage("line 3", () -> CourseFile.parse("size 400 300\ntarget 200 40 12\nlaunch 1\n"));
        assertMessage("line 4", () -> CourseFile.parse("size 400 300\ntarget 200 40 12\nlaunch 1 1\nlaunch 2 2\n"));
    }

    @Test
    void writeThenReadRoundTripsThroughAFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("sample.course");
        CourseFile.write(SAMPLE, file);
        assertEquals(SAMPLE, CourseFile.read(file));
    }

    @Test
    void errorsNameTheLine() {
        assertMessage("line 2", () -> CourseFile.parse("size 400 300\ntarget 1 2\n"));
        assertMessage("line 3", () -> CourseFile.parse("size 400 300\ntarget 1 2 3\nbarrier 1 x 3 4\n"));
        assertMessage("line 1", () -> CourseFile.parse("wall 1 2 3 4\n"));
        assertMessage("line 2", () -> CourseFile.parse("size 400 300\nsize 400 300\ntarget 1 2 3\n"));
    }

    @Test
    void structuralProblemsAreRejected() {
        assertMessage("missing 'size'", () -> CourseFile.parse("target 1 2 3\n"));
        assertMessage("missing 'target'", () -> CourseFile.parse("size 400 300\n"));
        assertMessage("outside", () -> CourseFile.parse("size 400 300\ntarget 500 10 5\n"));
        assertMessage("radius", () -> CourseFile.parse("size 400 300\ntarget 10 10 0\n"));
        assertMessage("barrier size", () -> CourseFile.parse("size 400 300\ntarget 10 10 5\nbarrier 0 0 0 5\n"));
    }

    @Test
    void readErrorsIncludeThePath(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.course");
        Files.writeString(file, "size 10 10\n");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CourseFile.read(file));
        assertTrue(e.getMessage().contains("broken.course"), e.getMessage());
    }

    @Test
    void bundledCourseFilesMatchTheBuiltInLayouts() throws IOException {
        assertEquals(CourseLayout.EASY.create(1024, 768), CourseFile.read(Path.of("courses/easy.course")));
        assertEquals(CourseLayout.CLASSIC.create(1024, 768), CourseFile.read(Path.of("courses/classic.course")));
    }

    private static void assertMessage(String expected, org.junit.jupiter.api.function.Executable call) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, call);
        assertTrue(e.getMessage().contains(expected), "message '" + e.getMessage() + "' lacks '" + expected + "'");
    }
}
