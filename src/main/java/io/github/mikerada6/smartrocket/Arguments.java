package io.github.mikerada6.smartrocket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Command-line options. Parsed by hand so the application has no runtime dependencies.
 *
 * @param courseFile          a course file that replaces the built-in course, including its world size
 * @param headlessGenerations when positive, run this many generations with no window and exit
 * @param seed                fixed random seed for a reproducible run, or empty for a random one
 * @param edit                open the course editor instead of running
 */
public record Arguments(SimulationConfig config, CourseLayout course, Optional<Path> courseFile, OptionalLong seed,
                        int headlessGenerations, Path logPath, boolean edit, boolean help) {

    public static final Path DEFAULT_LOG_PATH = Path.of("generations.csv");

    public static final String USAGE = """
            Usage: smartrocket [options]

            Options:
              --population N     rockets per generation (default 10000)
              --lifespan N       frames each generation lives (default 200)
              --width N          world width in pixels (default 1024)
              --height N         world height in pixels (default 768)
              --mutation-rate R  per-gene mutation probability, 0 to 1 (default 0.01)
              --max-speed R      speed limit in pixels per frame (default 14)
              --elite-fraction R share of the best rockets re-flown unchanged, 0 to 1 (default 0.01)
              --course NAME      EASY or CLASSIC (default EASY)
              --course-file PATH load the course from a file instead; its size line sets the
                                 world size, so --width and --height are not allowed with it
              --seed N           random seed for a reproducible run (default: random)
              --headless N       run N generations without a window, then exit
              --edit             start in the course editor; with --course-file it edits that file
                                 (created on save if it does not exist yet)
              --log PATH         CSV file for per-generation statistics (default generations.csv)
              --help             show this message
            """;

    public static Arguments parse(String[] args) {
        SimulationConfig defaults = SimulationConfig.defaults();
        int width = defaults.width();
        int height = defaults.height();
        int population = defaults.populationSize();
        int lifespan = defaults.lifespan();
        double mutationRate = defaults.mutationRate();
        double maxSpeed = defaults.maxSpeed();
        double eliteFraction = defaults.eliteFraction();
        CourseLayout course = CourseLayout.EASY;
        Optional<Path> courseFile = Optional.empty();
        boolean sizeGiven = false;
        OptionalLong seed = OptionalLong.empty();
        int headless = 0;
        Path logPath = DEFAULT_LOG_PATH;
        boolean edit = false;
        boolean help = false;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            if (flag.equals("--help") || flag.equals("-h")) {
                help = true;
                continue;
            }
            if (flag.equals("--edit")) {
                edit = true;
                continue;
            }
            String value = valueFor(flag, args, i++);
            switch (flag) {
                case "--population" -> population = parseInt(flag, value);
                case "--lifespan" -> lifespan = parseInt(flag, value);
                case "--width" -> {
                    width = parseInt(flag, value);
                    sizeGiven = true;
                }
                case "--height" -> {
                    height = parseInt(flag, value);
                    sizeGiven = true;
                }
                case "--mutation-rate" -> mutationRate = parseDouble(flag, value);
                case "--max-speed" -> maxSpeed = parseDouble(flag, value);
                case "--elite-fraction" -> eliteFraction = parseDouble(flag, value);
                case "--course" -> course = parseCourse(value);
                case "--course-file" -> courseFile = Optional.of(Path.of(value));
                case "--seed" -> seed = OptionalLong.of(parseLong(flag, value));
                case "--headless" -> headless = parseInt(flag, value);
                case "--log" -> logPath = Path.of(value);
                default -> throw new IllegalArgumentException("unknown option: " + flag);
            }
        }
        if (headless < 0) {
            throw new IllegalArgumentException("--headless must not be negative: " + headless);
        }
        if (courseFile.isPresent() && sizeGiven) {
            throw new IllegalArgumentException("--width and --height cannot be combined with --course-file; the file sets the size");
        }
        if (edit && headless > 0) {
            throw new IllegalArgumentException("--edit needs a window and cannot be combined with --headless");
        }
        SimulationConfig config = new SimulationConfig(width, height, population, lifespan, mutationRate, maxSpeed, eliteFraction);
        return new Arguments(config, course, courseFile, seed, headless, logPath, edit, help);
    }

    private static String valueFor(String flag, String[] args, int index) {
        if (!flag.startsWith("--")) {
            throw new IllegalArgumentException("unexpected argument: " + flag);
        }
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException(flag + " requires a value");
        }
        return args[index + 1];
    }

    private static int parseInt(String flag, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " expects a whole number, got: " + value);
        }
    }

    private static long parseLong(String flag, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " expects a whole number, got: " + value);
        }
    }

    private static double parseDouble(String flag, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " expects a number, got: " + value);
        }
    }

    private static CourseLayout parseCourse(String value) {
        try {
            return CourseLayout.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("--course must be EASY or CLASSIC, got: " + value);
        }
    }

    public boolean headless() {
        return headlessGenerations > 0;
    }

    /** The course file's world if one was given, otherwise the built-in course at the configured size. */
    public World loadWorld() throws IOException {
        if (courseFile.isPresent()) {
            return CourseFile.read(courseFile.get());
        }
        return course.create(config.width(), config.height());
    }

    /**
     * The world to start editing: the course file if it exists, otherwise the built-in
     * course, so a new file can be drawn from a sensible starting point.
     */
    public World loadWorldForEditing() throws IOException {
        if (courseFile.isPresent() && !Files.exists(courseFile.get())) {
            return course.create(config.width(), config.height());
        }
        return loadWorld();
    }

    public Random newRandom() {
        return seed.isPresent() ? new Random(seed.getAsLong()) : new Random();
    }
}
