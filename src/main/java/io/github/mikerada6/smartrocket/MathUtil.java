package io.github.mikerada6.smartrocket;

/** Small numeric helpers shared by the simulation classes. */
public final class MathUtil {

    private MathUtil() {
    }

    /**
     * Linearly maps {@code num} from the input range to the output range, extrapolating
     * outside the input range. The output range may be reversed (min greater than max).
     */
    public static double map(double num, double minInput, double maxInput, double minOutput, double maxOutput) {
        double slope = (maxOutput - minOutput) / (maxInput - minInput);
        return minOutput + slope * (num - minInput);
    }
}
