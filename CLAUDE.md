# SmartRocket

Stack profile: **Other** (plain Java, no framework). Java 25, Maven via the
checked-in wrapper, Swing for rendering, JUnit 5 for tests.

## Commands

- Build and test: `./mvnw verify`
- Run the app: `./mvnw exec:java`
- Tests only: `./mvnw test`

## Layout

- `src/main/java/io/github/mikerada6/smartrocket/` application code, single package for now
- `src/test/java/io/github/mikerada6/smartrocket/` unit tests, same package
- `docs/adr/` architecture decision records, numbered `NNNN-title.md`

## Conventions

- The default test lane is headless and offline. Surefire sets
  `java.awt.headless=true`; a test that needs a display or the network does
  not belong in it.
- Simulation logic must stay runnable without a window. Anything that reaches
  for Swing belongs in the rendering classes only.
- The compiler runs with `-Xlint:all`. Do not add warnings.
- Conventional Commits and GitFlow as in the global standards. PRs target `develop`.
- `generations.csv` (and the old `log.txt`) are runtime output, ignored and never committed.
