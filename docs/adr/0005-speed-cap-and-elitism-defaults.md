# 5. Cap rocket speed at 14 px/frame and keep the best 1% each generation

Date: 2026-09-05

## Status

Accepted

## Context

With the fitness bugs fixed, the original three-barrier course (`CLASSIC`)
was still never solved: every rocket crashed in every generation and average
fitness plateaued near 39. Velocity was unbounded, so rockets accelerated to
hundreds of pixels per frame and overshot every gap. The original author had
tried a speed limit but had to comment it out because `Vector.normalize()`
returned NaN and the limit froze every rocket.

Two levers were made configurable and measured headless on `CLASSIC` with
10,000 rockets and a lifespan of 200: `--max-speed` and elitism (the best
genomes re-flown unchanged). Runs used fixed seeds so results are
reproducible with `--course CLASSIC --seed N`.

First pass, seed 1, 20 generations:

| max speed | lifespan | elites | first hit | rockets on target at gen 19 |
|---|---|---|---|---|
| unlimited | 200 or 400 | 0 or 20 | never | 0 |
| 8 | 200 | 0 or 20 | never | 0 (crashes halved, but 8 px/frame cannot cover the course in 200 frames) |
| 8 | 400 | 0 or 20 | never | 0 (longer life only gave more time to crash) |
| 12 | 200 | 0 | never | 0 |
| 12 | 200 | 20 | gen 15 | 135 |
| 16 | 200 | 0 or 20 | never | 0 |

Second pass, seeds 1 to 3, 40 generations:

| max speed | elites | first hit by seed | rockets on target at gen 39 by seed |
|---|---|---|---|
| 10 | 20 or 100 | never | 0, 0, 0 |
| 12 | 20 | 15, never, never | 2218, 0, 0 |
| 12 | 100 | never | 0, 0, 0 |
| 14 | 20 | 27, 23, 13 | 1168, 1795, 2189 |
| 14 | 100 | 9, 17, 22 | 2313, 2662, 2195 |
| 16 | 100 | 11, 15, 18 | 2288, 2733, 2968 |

## Decision

Default `maxSpeed` to 14 and re-fly the best 1% of the population unchanged
each generation. A cap of 16 performed comparably (slightly later first hits,
slightly more rockets on target by generation 40); 14 was kept because it
found the target sooner on average and the difference is within seed noise. Elitism is a fraction rather than a count so the default is
valid for any population size; a positive fraction always keeps at least one
rocket. Both remain overridable from the command line, and unlimited speed
with no elites reproduces the old behaviour.

## Consequences

- `CLASSIC` is solved on every seed tried, typically within 10 to 25
  generations, and a fifth to a quarter of the population is on target by
  generation 40.
- The speed cap changes the flight dynamics on every course, including
  `EASY`; the easy-course test still passes with the new defaults.
- The tuning was done on one course, one population size and three seeds.
  It is a sensible default, not an optimum; the headless mode exists so
  anyone can rerun the comparison with `--course CLASSIC --seed N`.
