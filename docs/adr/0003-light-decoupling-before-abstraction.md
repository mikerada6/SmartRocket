# 3. Decouple simulation from the window before adding abstractions

Date: 2026-09-05

## Status

Accepted

## Context

Simulation state (frame count, age, generation, hit count, barriers, target)
lives in static fields on the Swing panel, and `Rocket` and `DNA` read them
directly. This makes the genetic algorithm untestable without constructing a
window and makes it impossible to run two simulations. Two remedies were
considered: a light version that moves the state into a plain simulation
object and passes dependencies through constructors, and a heavier version
that also introduces strategy interfaces for fitness, selection and mutation.

## Decision

Do the light version first. Move state into a headless simulation object,
inject a single seeded `Random`, and keep the existing concrete classes.
Introduce interfaces only when a second implementation actually exists.

## Consequences

- Tests can drive the simulation deterministically from a seed with no
  display, which is the prerequisite for every later change.
- The class structure stays recognisable, so the diff is reviewable and the
  behaviour change is limited to what the tests assert.
- Algorithm experiments (elitism, tournament selection, alternative fitness)
  are deferred. If they multiply, that is the signal to add the abstractions
  the heavier version proposed.
