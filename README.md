# SmartRocket

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
headless and offline. `exec:java` opens the simulation window. On Windows use
`mvnw.cmd` instead of `./mvnw`.

## Layout

```
src/main/java/io/github/mikerada6/smartrocket/   application code
src/test/java/io/github/mikerada6/smartrocket/   unit tests
docs/adr/                                         architecture decision records
```

## Contributing

Branches follow GitFlow: work happens on `feature/*` branches cut from
`develop`, and pull requests target `develop`. Commit messages follow
Conventional Commits (`feat:`, `fix:`, `build:`, `docs:`, `refactor:`, `test:`).
Non-obvious technical decisions are recorded in `docs/adr/`.
