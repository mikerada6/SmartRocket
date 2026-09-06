package io.github.mikerada6.smartrocket.world;

import io.github.mikerada6.smartrocket.geometry.Vec2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes courses as plain text so they can be authored, shared and edited
 * without touching code. One statement per line, {@code #} starts a comment:
 *
 * <pre>
 * size 1024 768          # world width and height in pixels
 * target 512 50 25       # centre x, centre y, radius
 * barrier 0 512 896 25   # left, top, width, height; any number of these
 * launch 512 743         # optional: where rockets start; bottom centre if absent
 * </pre>
 *
 * The format is deliberately not JSON: the JDK ships no JSON parser and the project
 * has no runtime dependencies.
 */
public final class CourseFile {

    private CourseFile() {
    }

    public static World read(Path path) throws IOException {
        try {
            return parse(Files.readString(path));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(path + ": " + e.getMessage(), e);
        }
    }

    public static void write(World world, Path path) throws IOException {
        Files.writeString(path, format(world));
    }

    public static World parse(String text) {
        Integer width = null;
        Integer height = null;
        Target target = null;
        Vec2 launch = null;
        List<Barrier> barriers = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = stripComment(lines[i]).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            switch (parts[0]) {
                case "size" -> {
                    if (width != null) {
                        throw error(lineNumber, "size given more than once");
                    }
                    requireArgs(lineNumber, parts, 2);
                    width = parseInt(lineNumber, parts[1]);
                    height = parseInt(lineNumber, parts[2]);
                }
                case "target" -> {
                    if (target != null) {
                        throw error(lineNumber, "target given more than once");
                    }
                    requireArgs(lineNumber, parts, 3);
                    target = new Target(new Vec2(parseDouble(lineNumber, parts[1]), parseDouble(lineNumber, parts[2])),
                            parseDouble(lineNumber, parts[3]));
                }
                case "launch" -> {
                    if (launch != null) {
                        throw error(lineNumber, "launch given more than once");
                    }
                    requireArgs(lineNumber, parts, 2);
                    launch = new Vec2(parseDouble(lineNumber, parts[1]), parseDouble(lineNumber, parts[2]));
                }
                case "barrier" -> {
                    requireArgs(lineNumber, parts, 4);
                    barriers.add(new Barrier(parseInt(lineNumber, parts[1]), parseInt(lineNumber, parts[2]),
                            parseInt(lineNumber, parts[3]), parseInt(lineNumber, parts[4])));
                }
                default -> throw error(lineNumber, "unknown statement '" + parts[0] + "', expected size, target, launch or barrier");
            }
        }
        if (width == null) {
            throw new IllegalArgumentException("missing 'size' line");
        }
        if (target == null) {
            throw new IllegalArgumentException("missing 'target' line");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("size must be positive, got " + width + " " + height);
        }
        if (target.radius() <= 0) {
            throw new IllegalArgumentException("target radius must be positive, got " + target.radius());
        }
        for (Barrier b : barriers) {
            if (b.width() <= 0 || b.height() <= 0) {
                throw new IllegalArgumentException("barrier size must be positive: " + b);
            }
        }
        World world = launch == null
                ? new World(width, height, target, barriers)
                : new World(width, height, target, barriers, launch);
        if (world.isOutOfBounds(target.centre())) {
            throw new IllegalArgumentException("target centre " + target.centre() + " lies outside the " + width + "x" + height + " world");
        }
        if (world.isOutOfBounds(world.launch())) {
            throw new IllegalArgumentException("launch point " + world.launch() + " lies outside the " + width + "x" + height + " world");
        }
        return world;
    }

    public static String format(World world) {
        StringBuilder out = new StringBuilder();
        out.append("# SmartRocket course: size W H; target X Y RADIUS; launch X Y; barrier LEFT TOP WIDTH HEIGHT\n");
        out.append("size ").append(world.width()).append(' ').append(world.height()).append('\n');
        Target t = world.target();
        out.append("target ").append(number(t.centre().x())).append(' ').append(number(t.centre().y()))
                .append(' ').append(number(t.radius())).append('\n');
        out.append("launch ").append(number(world.launch().x())).append(' ').append(number(world.launch().y())).append('\n');
        for (Barrier b : world.barriers()) {
            out.append("barrier ").append(b.x()).append(' ').append(b.y()).append(' ')
                    .append(b.width()).append(' ').append(b.height()).append('\n');
        }
        return out.toString();
    }

    /** Writes whole numbers without a trailing ".0" so files stay readable. */
    private static String number(double d) {
        return d == Math.rint(d) ? Long.toString((long) d) : Double.toString(d);
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash < 0 ? line : line.substring(0, hash);
    }

    private static void requireArgs(int lineNumber, String[] parts, int count) {
        if (parts.length != count + 1) {
            throw error(lineNumber, "'" + parts[0] + "' takes " + count + " numbers, got " + (parts.length - 1));
        }
    }

    private static int parseInt(int lineNumber, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw error(lineNumber, "expected a whole number, got '" + value + "'");
        }
    }

    private static double parseDouble(int lineNumber, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw error(lineNumber, "expected a number, got '" + value + "'");
        }
    }

    private static IllegalArgumentException error(int lineNumber, String message) {
        return new IllegalArgumentException("line " + lineNumber + ": " + message);
    }
}
