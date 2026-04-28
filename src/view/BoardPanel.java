package view;

import model.GaufreModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Panneau de la grille de la Gaufre.
 * Les cases se redimensionnent dynamiquement.
 * Animation de destruction.
 */
public class BoardPanel extends JPanel {

    private GaufreModel model;
    private int padding = 5;
    private int hoverRow = -1;
    private int hoverCol = -1;

    private static final Color WAFFLE_COLOR = new Color(235, 195, 80);
    private static final Color WAFFLE_DARK = new Color(200, 160, 50);
    private static final Color WAFFLE_BORDER = new Color(180, 140, 40);
    private static final Color POISON_COLOR = new Color(80, 180, 80);
    private static final Color POISON_DARK = new Color(50, 140, 50);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 100, 100, 120);
    private static final Color EATEN_COLOR = new Color(245, 240, 230);
    private static final Color BOARD_BG = new Color(250, 245, 235);

    private CellClickListener cellClickListener;

    private static final int ANIM_CELL_DURATION = 350;
    private static final int ANIM_CASCADE_DELAY = 40;
    private static final int PARTICLE_COUNT = 6;

    private final List<AnimatingCell> animatingCells = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private Timer animTimer;
    private final Random rng = new Random();
    private boolean animating = false;
    private String gameOverMessage = null;

    public void setGameOverMessage(String msg) {
        this.gameOverMessage = msg;
        repaint();
    }

    private static class AnimatingCell {
        int row, col;
        long startTime;
        float progress;

        AnimatingCell(int row, int col, long startTime) {
            this.row = row;
            this.col = col;
            this.startTime = startTime;
            this.progress = 0f;
        }
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float alpha;
        Color color;
        long startTime;
        int duration;

        Particle(float x, float y, float vx, float vy, float size, Color color, long startTime, int duration) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.size = size;
            this.alpha = 1f;
            this.color = color;
            this.startTime = startTime;
            this.duration = duration;
        }
    }

    public interface CellClickListener {
        void onCellClicked(int row, int col);
    }

    public BoardPanel(GaufreModel model) {
        this.model = model;
        setBackground(BOARD_BG);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                hoverCol = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleHover(e);
            }
        });
    }

    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    public void setModel(GaufreModel model) {
        this.model = model;
        repaint();
    }

    public boolean isAnimating() {
        return animating;
    }

    private int computeCellW() {
        if (model == null) return 80;
        int w = getWidth();
        if (w <= 0) return 80;
        int margin = 40;
        int availW = w - margin;
        int cw = (availW - padding * (model.getCols() - 1)) / model.getCols();
        return Math.max(cw, 30);
    }

    private int computeCellH() {
        if (model == null) return 50;
        int h = getHeight();
        if (h <= 0) return 50;
        int margin = 40;
        int availH = h - margin;
        int ch = (availH - padding * (model.getRows() - 1)) / model.getRows();
        return Math.max(ch, 20);
    }

    private int computeOffsetX(int cellW) {
        if (model == null) return 20;
        int totalW = model.getCols() * cellW + (model.getCols() - 1) * padding;
        return Math.max(20, (getWidth() - totalW) / 2);
    }

    private int computeOffsetY(int cellH) {
        if (model == null) return 20;
        int totalH = model.getRows() * cellH + (model.getRows() - 1) * padding;
        return Math.max(20, (getHeight() - totalH) / 2);
    }

    public void triggerDestructionAnimation(int clickRow, int clickCol, boolean[][] gridBefore) {
        if (model == null) return;
        int rows = gridBefore.length;
        int cols = gridBefore[0].length;

        long now = System.currentTimeMillis();
        animatingCells.clear();

        int cellW = computeCellW();
        int cellH = computeCellH();
        int offsetX = computeOffsetX(cellW);
        int offsetY = computeOffsetY(cellH);

        for (int i = clickRow; i < rows; i++) {
            for (int j = clickCol; j < cols; j++) {
                if (gridBefore[i][j]) {
                    int dist = (i - clickRow) + (j - clickCol);
                    long delay = (long) dist * ANIM_CASCADE_DELAY;
                    animatingCells.add(new AnimatingCell(i, j, now + delay));

                    float cx = offsetX + j * (cellW + padding) + cellW / 2f;
                    float cy = offsetY + i * (cellH + padding) + cellH / 2f;
                    spawnParticles(cx, cy, cellW, cellH, now + delay + ANIM_CELL_DURATION / 2);
                }
            }
        }

        if (!animatingCells.isEmpty()) {
            animating = true;
            startAnimTimer();
        }
    }

    private void spawnParticles(float cx, float cy, int cellW, int cellH, long startTime) {
        int avgSize = (cellW + cellH) / 2;
        for (int p = 0; p < PARTICLE_COUNT; p++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float speed = 1.5f + rng.nextFloat() * 3f;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed - 1f;
            float size = 3 + rng.nextFloat() * (avgSize / 8f);
            Color color = rng.nextBoolean() ? WAFFLE_COLOR : WAFFLE_DARK;
            int duration = 300 + rng.nextInt(300);
            particles.add(new Particle(cx, cy, vx, vy, size, color, startTime, duration));
        }
    }

    private void startAnimTimer() {
        if (animTimer != null && animTimer.isRunning()) return;
        animTimer = new Timer(16, e -> tickAnimation());
        animTimer.start();
    }

    private void tickAnimation() {
        long now = System.currentTimeMillis();

        Iterator<AnimatingCell> cellIt = animatingCells.iterator();
        while (cellIt.hasNext()) {
            AnimatingCell ac = cellIt.next();
            if (now < ac.startTime) {
                ac.progress = 0f;
            } else {
                float elapsed = now - ac.startTime;
                ac.progress = Math.min(1f, elapsed / ANIM_CELL_DURATION);
                if (ac.progress >= 1f) {
                    cellIt.remove();
                }
            }
        }

        Iterator<Particle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            Particle pt = partIt.next();
            if (now < pt.startTime) continue;
            float elapsed = now - pt.startTime;
            float t = elapsed / pt.duration;
            if (t >= 1f) {
                partIt.remove();
                continue;
            }
            pt.x += pt.vx;
            pt.y += pt.vy;
            pt.vy += 0.15f;
            pt.alpha = 1f - t;
        }

        repaint();

        if (animatingCells.isEmpty() && particles.isEmpty()) {
            animTimer.stop();
            animating = false;
        }
    }

    private void handleClick(MouseEvent e) {
        if (model == null || animating) return;
        int[] cell = getCellAt(e.getX(), e.getY());
        if (cell != null && cellClickListener != null) {
            cellClickListener.onCellClicked(cell[0], cell[1]);
        }
    }

    private void handleHover(MouseEvent e) {
        if (model == null) return;
        int[] cell = getCellAt(e.getX(), e.getY());
        int newRow = cell != null ? cell[0] : -1;
        int newCol = cell != null ? cell[1] : -1;
        if (newRow != hoverRow || newCol != hoverCol) {
            hoverRow = newRow;
            hoverCol = newCol;
            repaint();
        }
    }

    private int[] getCellAt(int x, int y) {
        int cellW = computeCellW();
        int cellH = computeCellH();
        int offsetX = computeOffsetX(cellW);
        int offsetY = computeOffsetY(cellH);
        for (int i = 0; i < model.getRows(); i++) {
            for (int j = 0; j < model.getCols(); j++) {
                int cx = offsetX + j * (cellW + padding);
                int cy = offsetY + i * (cellH + padding);
                if (x >= cx && x < cx + cellW && y >= cy && y < cy + cellH) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        if (model.isGameOver() && gameOverMessage != null && !animating) {
            drawGameOverMessage(g2);
            g2.dispose();
            return;
        }

        int cellW = computeCellW();
        int cellH = computeCellH();
        int offsetX = computeOffsetX(cellW);
        int offsetY = computeOffsetY(cellH);

        for (int i = 0; i < model.getRows(); i++) {
            for (int j = 0; j < model.getCols(); j++) {
                int x = offsetX + j * (cellW + padding);
                int y = offsetY + i * (cellH + padding);

                if (model.isCellPresent(i, j)) {
                    if (!isCellAnimating(i, j)) {
                        drawWaffleCell(g2, x, y, i, j, cellW, cellH);
                    } else {
                        drawEatenCell(g2, x, y, cellW, cellH);
                        drawAnimatingWaffleCell(g2, x, y, i, j, cellW, cellH);
                    }
                } else {
                    drawEatenCell(g2, x, y, cellW, cellH);
                    if (isCellAnimating(i, j)) {
                        drawAnimatingWaffleCell(g2, x, y, i, j, cellW, cellH);
                    }
                }
            }
        }

        drawParticles(g2);

        if (!animating && hoverRow >= 0 && hoverCol >= 0 && !model.isGameOver()
                && model.isCellPresent(hoverRow, hoverCol)) {
            drawHoverHighlight(g2, offsetX, offsetY, cellW, cellH);
        }

        g2.dispose();
    }

    private void drawGameOverMessage(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(gameOverMessage);
        int textH = fm.getAscent();
        
        int x = (getWidth() - textW) / 2;
        int y = (getHeight() + textH) / 2;
        
        // Ombre portée
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(gameOverMessage, x + 3, y + 3);
        
        // Couleur selon victoire/défaite
        if (gameOverMessage.toLowerCase().contains("gagn")) {
            g2.setColor(new Color(80, 200, 80)); // Vert
        } else if (gameOverMessage.toLowerCase().contains("perdu") || gameOverMessage.toLowerCase().contains("ia")) {
            g2.setColor(new Color(255, 80, 80)); // Rouge
        } else {
            g2.setColor(WAFFLE_DARK);
        }
        g2.drawString(gameOverMessage, x, y);
    }

    private boolean isCellAnimating(int row, int col) {
        for (AnimatingCell ac : animatingCells) {
            if (ac.row == row && ac.col == col) return true;
        }
        return false;
    }

    private AnimatingCell getAnimatingCell(int row, int col) {
        for (AnimatingCell ac : animatingCells) {
            if (ac.row == row && ac.col == col) return ac;
        }
        return null;
    }

    private void drawAnimatingWaffleCell(Graphics2D g2, int x, int y, int row, int col, int cellW, int cellH) {
        AnimatingCell ac = getAnimatingCell(row, col);
        if (ac == null || ac.progress <= 0f) {
            drawWaffleCell(g2, x, y, row, col, cellW, cellH);
            return;
        }

        float t = ac.progress;
        float easedT = t * t;

        float scale = 1f - easedT;
        float alpha = 1f - easedT;
        float rotation = easedT * 0.3f;

        if (scale <= 0.01f || alpha <= 0.01f) return;

        Graphics2D g2a = (Graphics2D) g2.create();
        g2a.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));

        float cx = x + cellW / 2f;
        float cy = y + cellH / 2f;

        AffineTransform transform = new AffineTransform();
        transform.translate(cx, cy);
        transform.rotate(rotation);
        transform.scale(scale, scale);
        transform.translate(-cx, -cy);
        g2a.setTransform(transform);

        drawWaffleCell(g2a, x, y, row, col, cellW, cellH);
        g2a.dispose();
    }

    private void drawParticles(Graphics2D g2) {
        long now = System.currentTimeMillis();
        for (Particle pt : particles) {
            if (now < pt.startTime || pt.alpha <= 0) continue;
            Graphics2D g2p = (Graphics2D) g2.create();
            g2p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, pt.alpha))));
            g2p.setColor(pt.color);
            int s = Math.max(1, (int) (pt.size * pt.alpha));
            g2p.fillRoundRect((int) pt.x - s / 2, (int) pt.y - s / 2, s, s, 2, 2);
            g2p.dispose();
        }
    }

    // === Cell drawing ===

    private void drawWaffleCell(Graphics2D g2, int x, int y, int row, int col, int cellW, int cellH) {
        int arcW = Math.max(4, cellW / 8);
        int arcH = Math.max(4, cellH / 6);
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellW, cellH, arcW, arcH);

        if (row == 0 && col == 0) {
            GradientPaint gp = new GradientPaint(x, y, POISON_COLOR, x + cellW, y + cellH, POISON_DARK);
            g2.setPaint(gp);
            g2.fill(rect);
            g2.setColor(POISON_DARK.darker());
            g2.setStroke(new BasicStroke(2));
            g2.draw(rect);

            g2.setColor(Color.WHITE);
            int fontSize = Math.max(12, Math.min(cellW, cellH) * 2 / 3);
            g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            String label = "X";
            int textX = x + (cellW - fm.stringWidth(label)) / 2;
            int textY = y + (cellH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, textX, textY);
        } else {
            GradientPaint gp = new GradientPaint(x, y, WAFFLE_COLOR, x + cellW, y + cellH, WAFFLE_DARK);
            g2.setPaint(gp);
            g2.fill(rect);
            g2.setColor(WAFFLE_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.draw(rect);

            // Waffle grid pattern
            g2.setColor(new Color(WAFFLE_BORDER.getRed(), WAFFLE_BORDER.getGreen(), WAFFLE_BORDER.getBlue(), 60));
            g2.setStroke(new BasicStroke(1));
            int thirdW = cellW / 3;
            int thirdH = cellH / 3;
            int insetW = Math.max(3, cellW / 12);
            int insetH = Math.max(3, cellH / 12);
            // Vertical lines
            g2.drawLine(x + thirdW, y + insetH, x + thirdW, y + cellH - insetH);
            g2.drawLine(x + 2 * thirdW, y + insetH, x + 2 * thirdW, y + cellH - insetH);
            // Horizontal lines
            g2.drawLine(x + insetW, y + thirdH, x + cellW - insetW, y + thirdH);
            g2.drawLine(x + insetW, y + 2 * thirdH, x + cellW - insetW, y + 2 * thirdH);
        }
    }

    private void drawEatenCell(Graphics2D g2, int x, int y, int cellW, int cellH) {
        int arcW = Math.max(4, cellW / 8);
        int arcH = Math.max(4, cellH / 6);
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellW, cellH, arcW, arcH);
        g2.setColor(EATEN_COLOR);
        g2.fill(rect);
        g2.setColor(new Color(220, 215, 205));
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{4, 4}, 0));
        g2.draw(rect);
    }

    private void drawHoverHighlight(Graphics2D g2, int offsetX, int offsetY, int cellW, int cellH) {
        int arcW = Math.max(4, cellW / 8);
        int arcH = Math.max(4, cellH / 6);
        for (int i = hoverRow; i < model.getRows(); i++) {
            for (int j = hoverCol; j < model.getCols(); j++) {
                if (model.isCellPresent(i, j)) {
                    int x = offsetX + j * (cellW + padding);
                    int y = offsetY + i * (cellH + padding);
                    RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellW, cellH, arcW, arcH);
                    g2.setColor(HIGHLIGHT_COLOR);
                    g2.fill(rect);
                    g2.setColor(new Color(255, 80, 80, 180));
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(rect);
                }
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
