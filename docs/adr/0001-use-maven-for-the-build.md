# 1. Use Maven for the build

Date: 2026-09-05

## Status

Accepted

## Context

The project originally had no build tool. Sources lived in a flat `src/`
directory in the default package and were compiled from an IDE. Reviving it
requires a reproducible build, a test runner, and a place to pin the Java
version. The realistic choices were Maven and Gradle. The project is nine
source files with a single test-scope dependency and no custom build logic.

## Decision

Use Maven with the checked-in wrapper (`mvnw`). Target Java 25 through
`maven.compiler.release`. Pin every plugin version explicitly in the pom.

## Consequences

- Anyone with a JDK can build with `./mvnw verify` and no other setup.
- The declarative pom is enough here; there is nothing to script. If the
  project later needs custom build steps, Gradle could be revisited, but the
  migration cost would be small because the layout is the standard one.
- Maven is the more conservative choice, so tooling and IDE support are
  reliable and the build file rarely needs attention.
