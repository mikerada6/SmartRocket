package io.github.mikerada6.smartrocket.world;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The course files in a directory. Unreadable files are skipped and
 * logged rather than failing the whole listing, so one bad file cannot hide the rest.
 */
public final class CourseLibrary {

    private static final Logger LOG = Logger.getLogger(CourseLibrary.class.getName());
    public static final String EXTENSION = ".course";

    /** One course in the library. */
    public record Entry(Path file, World world) {
        public String name() {
            String n = file.getFileName().toString();
            return n.endsWith(EXTENSION) ? n.substring(0, n.length() - EXTENSION.length()) : n;
        }

        public String summary() {
            return world.width() + "x" + world.height() + ", " + world.barriers().size()
                    + (world.barriers().size() == 1 ? " barrier" : " barriers");
        }
    }

    private CourseLibrary() {
    }

    /** All readable course files in the directory, sorted by name; empty if the directory is missing. */
    public static List<Entry> scan(Path directory) {
        List<Entry> entries = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return entries;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path file : files) {
                try {
                    entries.add(new Entry(file, CourseFile.read(file)));
                } catch (IOException | IllegalArgumentException e) {
                    LOG.log(Level.WARNING, "skipping unreadable course " + file + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not list courses in " + directory, e);
        }
        entries.sort(Comparator.comparing(Entry::name));
        return entries;
    }
}
