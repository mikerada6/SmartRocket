package io.github.mikerada6.smartrocket;

import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Command-line options. Parsed by hand so the application has no runtime dependencies.
 *
 * @param headlessGenerations when positive, run this many generations with no window and exit
 * @param seed                fixed random seed for a reproducible run, or empty for a random one
 */
public record Arguments(SimulationConfig config, CourseLayout course, OptionalLong seed,
                        int headlessGenerations, Path logPath, boolean help) {

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
              --seed N           random seed for a reproducible run (default: random)
              --headless N       run N generations without a window, then exit
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
        OptionalLong seed = OptionalLong.empty();
        int headless = 0;
        Path logPath = DEFAULT_LOG_PATH;
        boolean help = false;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            if (flag.equals("--help") || flag.equals("-h")) {
                help = true;
                continue;
            }
            String value = valueFor(flag, args, i++);
            switch (flag) {
                case "--population" -> population = parseInt(flag, value);
                case "--lifespan" -> lifespan = parseInt(flag, value);
                case "--width" -> width = parseInt(flag, value);
                case "--height" -> height = parseInt(flag, value);
                case "--mutation-rate" -> mutationRate = parseDouble(flag, value);
                case "--max-speed" -> maxSpeed = parseDouble(flag, value);
                case "--elite-fraction" -> eliteFraction = parseDouble(flag, value);
                case "--course" -> course = parseCourse(value);
                case "--seed" -> seed = OptionalLong.of(parseLong(flag, value));
                case "--headless" -> headless = parseInt(flag, value);
                case "--log" -> logPath = Path.of(value);
                default -> throw new IllegalArgumentException("unknown option: " + flag);
            }
        }
        if (headless < 0) {
            throw new IllegalArgumentException("--headless must not be negative: " + headless);
        }
        SimulationConfig config = new SimulationConfig(width, height, population, lifespan, mutationRate, maxSpeed, eliteFraction);
        return new Arguments(config, course, seed, headless, logPath, help);
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

    public World world() {
        return course.create(config.width(), config.height());
    }

    public Random newRandom() {
        return seed.isPresent() ? new Random(seed.getAsLong()) : new Random();
    }
}
