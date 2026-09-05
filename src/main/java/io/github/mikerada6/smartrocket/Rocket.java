package io.github.mikerada6.smartrocket;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Rocket implements Comparable<Rocket> {

    /** Magnitude of every gene's thrust vector. */
    public static final double MAX_THRUST = 4;
    private static final int ROCKET_HEIGHT = 25;
    private static final int ROCKET_WIDTH = 5;

    private final World world;
    private final DNA dna;
    private Vector pos;
    private Vector vel;
    private Vector acc;
    private boolean hitTarget;
    private boolean crashed;
    private double matingEligibility;
    private int stopTime;

    public Rocket(DNA dna, World world) {
        this.dna = dna;
        this.world = world;
        pos = new Vector(world.width() / 2, world.height() - ROCKET_HEIGHT);
        vel = new Vector(0, 0);
        acc = new Vector(0, 0);
        hitTarget = false;
        crashed = false;
        matingEligibility = 0;
        stopTime = -1;
    }

    /**
     * Advances the rocket one frame using the gene for the given age.
     *
     * @return true if the rocket is on the target after this frame
     */
    public boolean update(int age) {
        Target target = world.target();
        if (distanceToTarget() < target.getSize()) {
            hitTarget = true;
            pos = target.getPos().copy();
        }

        if (world.isOutOfBounds(pos)) {
            crashed = true;
        }
        applyForce(dna.getGene(age));
        if (!hitTarget && !crashed) {
            vel = vel.add(acc);
            pos = pos.add(vel);
            acc = acc.multiply(0);
        }
        if (hitTarget && stopTime == -1) {
            stopTime = age;
        }
        return hitTarget;
    }

    public void applyForce(Vector v) {
        this.acc = acc.add(v);
    }

    public double distanceToTarget() {
        return pos.dist(world.target().getPos());
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

    public Graphics draw(Graphics g) {
        AffineTransform transform = new AffineTransform();
        Graphics2D g2 = (Graphics2D) g;
        double theta = pos.getAngleRadians();

        transform.rotate(theta, this.pos.getX() + ROCKET_WIDTH / 2, this.pos.getY() + ROCKET_HEIGHT / 2);
        AffineTransform old = g2.getTransform();
        g2.transform(transform);
        g2.setColor(dna.getColor());
        g2.fillRect((int) this.pos.getX(), (int) this.pos.getY(), ROCKET_WIDTH, ROCKET_HEIGHT);
        g2.setTransform(old);

        return g;
    }

    public double getXPos() {
        return this.pos.getX();
    }

    public double getYPos() {
        return this.pos.getY();
    }

    public boolean hasHitTarget() {
        return hitTarget;
    }

    public boolean hasCrashed() {
        return crashed;
    }

    public double getMatingEligibility() {
        return matingEligibility;
    }

    public void setMatingEligibility(double matingEligibility) {
        this.matingEligibility = matingEligibility;
    }

    public DNA getDna() {
        return dna;
    }

    /** Marks the rocket crashed if it overlaps any barrier in its world. */
    public boolean checkBarriers() {
        Rectangle me = new Rectangle((int) this.pos.getX(), (int) this.pos.getY(), ROCKET_WIDTH, ROCKET_HEIGHT);
        for (Barrier b : world.barriers()) {
            Rectangle wall = new Rectangle(b.getLeft(), b.getTop(), b.getWidth(), b.getHeight());
            if (me.intersects(wall)) {
                crashed = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rocket o) {
        return Double.compare(this.calcFitness(), o.calcFitness());
    }
}
