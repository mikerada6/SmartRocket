# 4. Ship an easy course as the default and keep the original as a benchmark

Date: 2026-09-05

## Status

Accepted

## Context

The original program hard-coded a three-barrier course. Headless runs with
the fitness bugs fixed show the current genetic algorithm never solves it:
all 10,000 rockets crash in every generation and average fitness plateaus
near 39, the score of a rocket that dies just below the first barrier.
Velocity is unbounded, so rockets overshoot the gaps. While the code is being
reworked, a default course nobody can watch being solved makes it impossible
to tell a rendering or loop regression from the algorithm's known weakness.

## Decision

Introduce named layouts in `CourseLayout`. `EASY` is one barrier at mid
height with a gap on the right; a random population lands hits in its first
generation and reaches roughly two thirds hitting by generation 30. It is the
default. `CLASSIC` is the original course, unchanged, and remains available
for the algorithm work.

## Consequences

- The GUI shows visible learning, so smoke tests and demos are meaningful.
- Algorithm changes are measured against `CLASSIC`, which is the course that
  actually discriminates between approaches.
- The layout is a value rather than code, so a later config or CLI option can
  select it without touching the simulation classes.
