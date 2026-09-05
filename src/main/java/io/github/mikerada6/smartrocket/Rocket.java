package io.github.mikerada6.smartrocket;

import java.awt.Color;

/** One rocket flying a genome through a world. Holds no rendering code. */
public final class Rocket {

    /** Magnitude of every gene's thrust vector. */
    public static final double MAX_THRUST = 4;
    /** Collision box and drawn size, in world units. */
    public static final int WIDTH = 5;
    public static final int HEIGHT = 25;

    private final World world;
    private final DNA dna;
    private Vec2 pos;
    private Vec2 vel = Vec2.ZERO;
    private Vec2 acc = Vec2.ZERO;
    private boolean hitTarget;
    private boolean crashed;
    private double fitness;
    private int stopTime = -1;

    public Rocket(DNA dna, World world) {
        this.dna = dna;
        this.world = world;
        pos = new Vec2(world.width() / 2.0, world.height() - HEIGHT);
    }

    /**
     * Advances the rocket one frame using the gene for the given age.
     *
     * @return true if the rocket is on the target after this frame
     */
    public boolean update(int age) {
        Target target = world.target();
        if (target.contains(pos)) {
            hitTarget = true;
            pos = target.centre();
        }
        if (world.isOutOfBounds(pos)) {
            crashed = true;
        }
        acc = acc.add(dna.getGene(age));
        if (!hitTarget && !crashed) {
            vel = vel.add(acc);
            pos = pos.add(vel);
        }
        acc = Vec2.ZERO;
        if (hitTarget && stopTime == -1) {
            stopTime = age;
        }
        return hitTarget;
    }

    public double distanceToTarget() {
        return pos.dist(world.target().centre());
    }

    /** Computes fitness for the current state, caches it, and returns it. */
    public double evaluateFitness() {
        fitness = calcFitness();
        return fitness;
    }

    /** Fitness as of the last {@link #evaluateFitness()} call, 0 before the first. */
    public double fitness() {
        return fitness;
    }

    public double calcFitness() {
        double d = distanceToTarget();
        double fitness = MathUtil.map(d, 0, world.width(), world.height(), 0);
        if (hitTarget) {
            fitness *= 10;
        } else if (crashed) {
            fitness /= 10;
        }
        if (stopTime != -1) {
            fitness = fitness + MathUtil.map(stopTime, 0, dna.length(), 20000, 0);
        }
        return fitness;
    }

    /** Marks the rocket crashed if its collision box overlaps any barrier in its world. */
    public boolean checkBarriers() {
        for (Barrier b : world.barriers()) {
            if (b.overlaps(pos.x(), pos.y(), WIDTH, HEIGHT)) {
                crashed = true;
                return true;
            }
        }
        return false;
    }

    /** Top-left corner of the rocket's collision box. */
    public Vec2 position() {
        return pos;
    }

    public Vec2 velocity() {
        return vel;
    }

    /** Direction of travel in radians from the +x axis; straight up when not moving. */
    public double heading() {
        return vel.equals(Vec2.ZERO) ? -Math.PI / 2 : vel.heading();
    }

    public Color color() {
        return dna.getColor();
    }

    public boolean hasHitTarget() {
        return hitTarget;
    }

    public boolean hasCrashed() {
        return crashed;
    }

    public DNA getDna() {
        return dna;
    }
}
