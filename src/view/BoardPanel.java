package view;

import model.GaufreModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panneau de la grille de la Gaufre (Vue du plateau).
 * Les cases se redimensionnent dynamiquement pour occuper tout l'espace disponible.
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

    private int computeCellSize() {
        if (model == null) return 60;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return 60;

        int margin = 40;
        int availW = w - margin;
        int availH = h - margin;

        int cellW = (availW - padding * (model.getCols() - 1)) / model.getCols();
        int cellH = (availH - padding * (model.getRows() - 1)) / model.getRows();

        int cellSize = Math.min(cellW, cellH);
        return Math.max(cellSize, 20); // minimum 20px
    }

    /**
     * Calcule l'offset X pour centrer la grille horizontalement.
     */
    private int computeOffsetX(int cellSize) {
        if (model == null) return 20;
        int totalW = model.getCols() * cellSize + (model.getCols() - 1) * padding;
        return Math.max(20, (getWidth() - totalW) / 2);
    }

    /**
     * Calcule l'offset Y pour centrer la grille verticalement.
     */
    private int computeOffsetY(int cellSize) {
        if (model == null) return 20;
        int totalH = model.getRows() * cellSize + (model.getRows() - 1) * padding;
        return Math.max(20, (getHeight() - totalH) / 2);
    }

    private void handleClick(MouseEvent e) {
        if (model == null) return;
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
        int cellSize = computeCellSize();
        int offsetX = computeOffsetX(cellSize);
        int offsetY = computeOffsetY(cellSize);
        for (int i = 0; i < model.getRows(); i++) {
            for (int j = 0; j < model.getCols(); j++) {
                int cx = offsetX + j * (cellSize + padding);
                int cy = offsetY + i * (cellSize + padding);
                if (x >= cx && x < cx + cellSize && y >= cy && y < cy + cellSize) {
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

        int cellSize = computeCellSize();
        int offsetX = computeOffsetX(cellSize);
        int offsetY = computeOffsetY(cellSize);

        for (int i = 0; i < model.getRows(); i++) {
            for (int j = 0; j < model.getCols(); j++) {
                int x = offsetX + j * (cellSize + padding);
                int y = offsetY + i * (cellSize + padding);

                if (model.isCellPresent(i, j)) {
                    drawWaffleCell(g2, x, y, i, j, cellSize);
                } else {
                    drawEatenCell(g2, x, y, cellSize);
                }
            }
        }

        if (hoverRow >= 0 && hoverCol >= 0 && !model.isGameOver()
                && model.isCellPresent(hoverRow, hoverCol)) {
            drawHoverHighlight(g2, offsetX, offsetY, cellSize);
        }

        g2.dispose();
    }

    private void drawWaffleCell(Graphics2D g2, int x, int y, int row, int col, int cellSize) {
        int arc = Math.max(4, cellSize / 6);
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellSize, cellSize, arc, arc);

        if (row == 0 && col == 0) {
            GradientPaint gp = new GradientPaint(x, y, POISON_COLOR, x + cellSize, y + cellSize, POISON_DARK);
            g2.setPaint(gp);
            g2.fill(rect);
            g2.setColor(POISON_DARK.darker());
            g2.setStroke(new BasicStroke(2));
            g2.draw(rect);

            // Texte "X" pour le poison (compatible multi-plateforme)
            g2.setColor(Color.WHITE);
            int fontSize = Math.max(12, cellSize * 2 / 3);
            g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            String label = "X";
            int textX = x + (cellSize - fm.stringWidth(label)) / 2;
            int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, textX, textY);
        } else {
            GradientPaint gp = new GradientPaint(x, y, WAFFLE_COLOR, x + cellSize, y + cellSize, WAFFLE_DARK);
            g2.setPaint(gp);
            g2.fill(rect);
            g2.setColor(WAFFLE_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.draw(rect);

            // Motif gaufre
            g2.setColor(new Color(WAFFLE_BORDER.getRed(), WAFFLE_BORDER.getGreen(), WAFFLE_BORDER.getBlue(), 60));
            g2.setStroke(new BasicStroke(1));
            int third = cellSize / 3;
            int inset = Math.max(3, cellSize / 12);
            g2.drawLine(x + third, y + inset, x + third, y + cellSize - inset);
            g2.drawLine(x + 2 * third, y + inset, x + 2 * third, y + cellSize - inset);
            g2.drawLine(x + inset, y + third, x + cellSize - inset, y + third);
            g2.drawLine(x + inset, y + 2 * third, x + cellSize - inset, y + 2 * third);
        }
    }

    private void drawEatenCell(Graphics2D g2, int x, int y, int cellSize) {
        int arc = Math.max(4, cellSize / 6);
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellSize, cellSize, arc, arc);
        g2.setColor(EATEN_COLOR);
        g2.fill(rect);
        g2.setColor(new Color(220, 215, 205));
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{4, 4}, 0));
        g2.draw(rect);
    }

    private void drawHoverHighlight(Graphics2D g2, int offsetX, int offsetY, int cellSize) {
        int arc = Math.max(4, cellSize / 6);
        for (int i = hoverRow; i < model.getRows(); i++) {
            for (int j = hoverCol; j < model.getCols(); j++) {
                if (model.isCellPresent(i, j)) {
                    int x = offsetX + j * (cellSize + padding);
                    int y = offsetY + i * (cellSize + padding);
                    RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, cellSize, cellSize, arc, arc);
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
