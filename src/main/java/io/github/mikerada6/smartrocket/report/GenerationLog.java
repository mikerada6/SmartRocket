package io.github.mikerada6.smartrocket.report;

import io.github.mikerada6.smartrocket.simulation.GenerationStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Appends one CSV line per completed generation. The file stays open for the life of
 * the run and is flushed after each line, so a crash loses at most the current
 * generation. A write failure is logged and the simulation carries on; the old code
 * deliberately divided by zero to abort the game thread instead.
 */
public final class GenerationLog implements Consumer<GenerationStats>, AutoCloseable {

    private static final Logger LOG = Logger.getLogger(GenerationLog.class.getName());

    private final Path path;
    private final BufferedWriter writer;

    public GenerationLog(Path path) throws IOException {
        this.path = path;
        this.writer = Files.newBufferedWriter(path);
        writer.write(GenerationStats.CSV_HEADER);
        writer.newLine();
        writer.flush();
    }

    @Override
    public void accept(GenerationStats stats) {
        try {
            writer.write(stats.toCsv());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not append generation " + stats.generation() + " to " + path, e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
