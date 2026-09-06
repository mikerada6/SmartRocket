package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the package layering honest by reading imports straight from the sources, so no
 * dependency-analysis library is needed. The rule: geometry depends on nothing, world on
 * geometry, simulation and report on those, cli on all of them, and only ui and the entry
 * point may touch Swing or AWT. See ADR 0007.
 */
class PackageDependencyTest {

    private static final String ROOT = "io.github.mikerada6.smartrocket.";
    private static final Path SOURCES = Path.of("src/main/java/io/github/mikerada6/smartrocket");

    /** Package name to the prefixes it must never import. */
    private static final Map<String, List<String>> FORBIDDEN = Map.of(
            "geometry", List.of(ROOT + "world", ROOT + "simulation", ROOT + "report", ROOT + "cli", ROOT + "ui", "java.awt", "javax.swing"),
            "world", List.of(ROOT + "simulation", ROOT + "report", ROOT + "cli", ROOT + "ui", "java.awt", "javax.swing"),
            "simulation", List.of(ROOT + "report", ROOT + "cli", ROOT + "ui", "java.awt", "javax.swing"),
            "report", List.of(ROOT + "cli", ROOT + "ui", "java.awt", "javax.swing"),
            "cli", List.of(ROOT + "ui", "java.awt", "javax.swing"));

    @Test
    void lowerLayersNeverImportHigherOnesOrTheToolkit() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map.Entry<String, List<String>> rule : FORBIDDEN.entrySet()) {
            Path dir = SOURCES.resolve(rule.getKey());
            assertTrue(Files.isDirectory(dir), "missing package directory " + dir);
            try (Stream<Path> files = Files.walk(dir)) {
                for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                    for (String line : Files.readAllLines(file)) {
                        if (!line.startsWith("import ")) {
                            continue;
                        }
                        for (String forbidden : rule.getValue()) {
                            if (line.startsWith("import " + forbidden) || line.startsWith("import static " + forbidden)) {
                                violations.add(SOURCES.relativize(file) + ": " + line.trim());
                            }
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "layering violations:\n" + String.join("\n", violations));
    }
}
