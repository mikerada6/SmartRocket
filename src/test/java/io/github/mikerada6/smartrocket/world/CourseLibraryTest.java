package io.github.mikerada6.smartrocket.world;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseLibraryTest {

    @Test
    void listsReadableCoursesSortedByNameAndSkipsBadOnes(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("zeta.course"), "size 300 200\ntarget 150 30 10\nbarrier 0 100 100 10\n");
        Files.writeString(dir.resolve("alpha.course"), "size 300 200\ntarget 150 30 10\n");
        Files.writeString(dir.resolve("broken.course"), "size 300\n");
        Files.writeString(dir.resolve("notes.txt"), "size 300 200\ntarget 150 30 10\n");

        List<CourseLibrary.Entry> entries = CourseLibrary.scan(dir);

        assertEquals(List.of("alpha", "zeta"), entries.stream().map(CourseLibrary.Entry::name).toList());
        assertEquals("300x200, 0 barriers", entries.get(0).summary());
        assertEquals("300x200, 1 barrier", entries.get(1).summary());
    }

    @Test
    void missingDirectoryIsEmptyNotAnError(@TempDir Path dir) {
        assertTrue(CourseLibrary.scan(dir.resolve("nowhere")).isEmpty());
    }

    @Test
    void bundledCoursesAreListed() {
        List<String> names = CourseLibrary.scan(Path.of("courses")).stream().map(CourseLibrary.Entry::name).toList();
        assertTrue(names.contains("easy") && names.contains("classic"), names.toString());
    }
}
