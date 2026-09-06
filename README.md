# SmartRocket

[![CI](https://github.com/mikerada6/SmartRocket/actions/workflows/ci.yml/badge.svg)](https://github.com/mikerada6/SmartRocket/actions/workflows/ci.yml)

A small genetic-algorithm demo: a population of rockets, each driven by a fixed
sequence of thrust vectors (its DNA), tries to reach a target while avoiding
barriers. After each generation the fittest rockets are bred and mutated, and
the population gradually learns a path. Rendering is plain Java Swing.

## Requirements

- JDK 25 or newer on `PATH` (the build targets Java 25).
- Nothing else. The Maven wrapper downloads Maven itself on first use.

## Build, test, run

```bash
./mvnw verify
```

```bash
./mvnw exec:java
```

`verify` compiles with all lint warnings enabled and runs the unit tests
headless and offline. `exec:java` opens the simulation window.

Options go after `-Dexec.args=`. For example, twenty generations on the
original course with a fixed seed and no window:

```bash
./mvnw -q exec:java -Dexec.args="--headless 20 --course CLASSIC --seed 42"
```

| Option | Meaning | Default |
|---|---|---|
| `--population N` | rockets per generation | 10000 |
| `--lifespan N` | frames each generation lives | 200 |
| `--width N`, `--height N` | world size in pixels | 1024 x 768 |
| `--mutation-rate R` | per-gene mutation probability | 0.01 |
| `--max-speed R` | speed limit in pixels per frame | 14 |
| `--elite-fraction R` | share of the best rockets re-flown unchanged each generation | 0.01 |
| `--course NAME` | `EASY` or `CLASSIC` | `EASY` |
| `--course-file PATH` | load a course from a file; its size line sets the world size | none |
| `--seed N` | random seed for a reproducible run | random |
| `--headless N` | run N generations without a window, then exit | off |
| `--edit` | open the course editor instead of running | off |
| `--log PATH` | CSV file for per-generation statistics | `generations.csv` | Each run writes
one CSV line per generation to `generations.csv` in the working directory
(average and best fitness, rockets that hit the target, rockets that crashed,
and the age of the first hit). On Windows use
`mvnw.cmd` instead of `./mvnw`.

## The window

The toolbar has Pause, Restart, a speed slider (simulation steps per drawn
frame, so 100x runs half a generation between frames), a course picker with
the built-in courses and any file you open, and an Edit course toggle that
swaps the simulation for the editor in the same window. The run pauses while
you edit; switching back, or pressing Run in the editor, restarts on the
course as drawn. Parameters opens a side panel where population, lifespan,
mutation rate, speed limit and elite share can be changed; Apply restarts
with the new values. Help lists every control and shortcut. The window can be
resized and the course scales to fit; its size, speed setting and whether the
parameters are shown are remembered between runs. The panel on the
right charts average and best fitness and the number of rockets on target or
crashed for every generation of the current run.

Rockets are coloured by state: blue when far from the target shading to
orange as they approach, white once on the target, grey after a crash. The
best rockets of the previous generation, re-flown unchanged, are outlined in
white and leave a trail.

## Custom courses

A course is a small text file. `#` starts a comment; `size` and `target`
appear once, `barrier` any number of times:

```
size 1024 768          # world width and height in pixels
target 512 50 25       # centre x, centre y, radius
launch 512 743         # optional: where rockets start (bottom centre if absent)
barrier 0 512 896 25   # left, top, width, height
```

Run it with `--course-file`. The `courses/` directory holds the two built-in
courses in this format as starting points:

```bash
./mvnw -q exec:java -Dexec.args="--course-file courses/classic.course"
```

Or draw one with the Edit course toggle in the window. `--edit` starts in the
editor, on the built-in course or on the given file if it exists:

```bash
./mvnw -q exec:java -Dexec.args="--edit --course-file courses/mine.course"
```

Drag on empty space to draw a barrier; drag a barrier to move it or drag one
of its edges or corners to resize it; drag the target or the launch marker to
move them; click a barrier and press Delete to remove it; scroll over the
target to resize it. Snap to grid is on by default and can be toggled in the
toolbar. Save writes the file; Run restarts the simulation on the course as
drawn, and you can keep editing and run again.

## Layout

```
src/main/java/io/github/mikerada6/smartrocket/
  geometry/     vectors and numeric helpers
  world/        world, barriers, target, courses and their files
  simulation/   the genetic algorithm and per-generation statistics
  report/       CSV log and chart history
  cli/          command-line options
  ui/           the Swing window, renderer, panels; ui/editor holds the course editor
  SmartRockets  entry point
src/test/java/...                                 unit tests, mirroring the packages
docs/adr/                                         architecture decision records
courses/                                          example course files
```

Lower packages never import higher ones, and only `ui` touches Swing, so the
simulation runs headless by construction (see ADR 0007).

## Contributing

Branches follow GitFlow: work happens on `feature/*` branches cut from
`develop`, and pull requests target `develop`. Commit messages follow
Conventional Commits (`feat:`, `fix:`, `build:`, `docs:`, `refactor:`, `test:`).
Non-obvious technical decisions are recorded in `docs/adr/`.
