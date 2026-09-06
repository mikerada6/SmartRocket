package io.github.mikerada6.smartrocket.ui;

import io.github.mikerada6.smartrocket.report.StatsHistory;
import io.github.mikerada6.smartrocket.simulation.GenerationStats;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.ToDoubleFunction;

/** Live charts of the run: fitness and outcome counts per generation, plus the latest numbers. */
public final class StatsPanel extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final Color AVERAGE = new Color(93, 173, 226);
    private static final Color MAX = new Color(244, 208, 63);
    private static final Color HITS = SimulationRenderer.TARGET;
    private static final Color CRASHES = SimulationRenderer.BARRIER;
    private static final Color GRID = new Color(60, 60, 68);
    private static final int MARGIN = 12;
    private static final int LEGEND_HEIGHT = 22;
    private static final int SUMMARY_HEIGHT = 56;

    private final transient StatsHistory history = new StatsHistory();
    private int populationSize = 1;

    public StatsPanel() {
        setPreferredSize(new Dimension(320, 400));
        setBackground(SimulationRenderer.BACKGROUND);
        setOpaque(true);
    }

    public StatsHistory history() {
        return history;
    }

    /** Forgets the previous run; the population size scales the outcome chart. */
    public void reset(int populationSize) {
        this.populationSize = Math.max(1, populationSize);
        history.clear();
        repaint();
    }

    public void accept(GenerationStats stats) {
        history.add(stats);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Each chart also needs its legend row, and the summary needs three text lines below.
        int chartHeight = (getHeight() - 4 * MARGIN - 2 * LEGEND_HEIGHT - SUMMARY_HEIGHT) / 2;
        int y = MARGIN;
        y = drawChart(g2, y, chartHeight, "Fitness per generation", history.max(GenerationStats::maxFitness),
                List.of(new Series("average", AVERAGE, GenerationStats::averageFitness),
                        new Series("best", MAX, GenerationStats::maxFitness)));
        y = drawChart(g2, y + MARGIN, chartHeight, "Rockets per generation", populationSize,
                List.of(new Series("on target", HITS, GenerationStats::hitRockets),
                        new Series("crashed", CRASHES, GenerationStats::crashedRockets)));
        drawSummary(g2, y + MARGIN);
    }

    private record Series(String name, Color color, ToDoubleFunction<GenerationStats> value) {
    }

    private int drawChart(Graphics2D g, int top, int height, String title, double maxValue, List<Series> series) {
        int left = MARGIN;
        int right = getWidth() - MARGIN;
        int bottom = top + height;
        g.setColor(SimulationRenderer.HUD);
        g.drawString(title, left, top + 12);
        int plotTop = top + 20;
        g.setColor(GRID);
        g.drawRect(left, plotTop, right - left, bottom - plotTop);
        for (int i = 1; i < 4; i++) {
            int gy = plotTop + (bottom - plotTop) * i / 4;
            g.drawLine(left, gy, right, gy);
        }
        List<GenerationStats> data = history.all();
        if (data.size() >= 2 && maxValue > 0) {
            for (Series s : series) {
                g.setColor(s.color());
                int prevX = -1;
                int prevY = -1;
                for (int i = 0; i < data.size(); i++) {
                    int x = left + (right - left) * i / (data.size() - 1);
                    double v = Math.max(0, Math.min(maxValue, s.value().applyAsDouble(data.get(i))));
                    int yy = bottom - (int) Math.round((bottom - plotTop) * v / maxValue);
                    if (prevX >= 0) {
                        g.drawLine(prevX, prevY, x, yy);
                    }
                    prevX = x;
                    prevY = yy;
                }
            }
        }
        int legendX = left;
        for (Series s : series) {
            g.setColor(s.color());
            g.fillRect(legendX, bottom + 6, 10, 10);
            g.setColor(SimulationRenderer.HUD);
            g.drawString(s.name(), legendX + 14, bottom + 15);
            legendX += 14 + g.getFontMetrics().stringWidth(s.name()) + 16;
        }
        g.setColor(GRID);
        g.drawString(formatValue(maxValue), right - 40, plotTop + 10);
        return bottom + LEGEND_HEIGHT;
    }

    private void drawSummary(Graphics2D g, int top) {
        g.setColor(SimulationRenderer.HUD);
        int y = top + 12;
        if (history.isEmpty()) {
            g.drawString("Waiting for the first generation to finish...", MARGIN, y);
            return;
        }
        GenerationStats s = history.latest();
        g.drawString(String.format("Generation %d: avg %.0f, best %.0f", s.generation(), s.averageFitness(), s.maxFitness()), MARGIN, y);
        g.drawString(String.format("%d on target, %d crashed, %d flying", s.hitRockets(), s.crashedRockets(),
                populationSize - s.hitRockets() - s.crashedRockets()), MARGIN, y += 16);
        int first = history.firstGenerationWithHit();
        g.drawString(first < 0 ? "No hit yet" : "First hit in generation " + first
                + (s.firstHitAge() >= 0 ? ", latest at age " + s.firstHitAge() : ""), MARGIN, y += 16);
    }

    private static String formatValue(double v) {
        return v >= 1000 ? String.format("%.0fk", v / 1000) : String.format("%.0f", v);
    }
}
