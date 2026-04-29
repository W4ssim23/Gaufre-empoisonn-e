package view;

import model.GameConfig;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ConfigPanel extends JPanel {

    public interface ConfigListener {
        void onConfigConfirmed(GameConfig config);
    }

    private JSpinner rowsSpinner, colsSpinner;
    private JRadioButton humanVsHuman, humanVsAI, humanStarts, aiStarts;
    private JPanel aiOptionsPanel;
    private ConfigListener listener;

    // Catppuccin Macchiato inspired palette
    private static final Color BG = new Color(36, 39, 58); // Mocha Base
    private static final Color PANEL_BG = new Color(49, 50, 68); // Surface0
    private static final Color ACCENT = new Color(249, 226, 175); // Yellow
    private static final Color ACCENT_HOVER = new Color(242, 205, 132); // Darker Yellow
    private static final Color TXT_MAIN = new Color(205, 214, 244); // Text
    private static final Color TXT_SEC = new Color(166, 173, 200); // Subtext0
    private static final Color BORDER_C = new Color(69, 71, 90); // Surface1

    public ConfigPanel() {
        setBackground(BG);
        setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Gaufre Empoisonnée");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        inner.add(title);

        JLabel subtitle = new JLabel("Configuration de la partie");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TXT_SEC);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(5, 0, 30, 0));
        inner.add(subtitle);

        // Taille
        JPanel sizeContent = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0;
        sizeContent.add(lbl("Lignes (N):"), g);
        g.gridx = 1;
        rowsSpinner = spinner(5, 2, 10);
        sizeContent.add(rowsSpinner, g);
        g.gridx = 0; g.gridy = 1;
        sizeContent.add(lbl("Colonnes (M):"), g);
        g.gridx = 1;
        colsSpinner = spinner(7, 2, 12);
        sizeContent.add(colsSpinner, g);
        inner.add(styledPanel("Taille de la grille", sizeContent));
        inner.add(Box.createVerticalStrut(20));

        // Mode
        JPanel modeContent = new JPanel();
        modeContent.setLayout(new BoxLayout(modeContent, BoxLayout.Y_AXIS));
        modeContent.setBorder(new EmptyBorder(0, 15, 15, 15));
        humanVsHuman = radio("Humain vs Humain");
        humanVsAI = radio("Humain vs IA");
        humanVsAI.setSelected(true);
        ButtonGroup mg = new ButtonGroup();
        mg.add(humanVsHuman);
        mg.add(humanVsAI);
        modeContent.add(humanVsHuman);
        modeContent.add(Box.createVerticalStrut(8));
        modeContent.add(humanVsAI);
        inner.add(styledPanel("Mode de jeu", modeContent));
        inner.add(Box.createVerticalStrut(20));

        // Options IA
        aiOptionsPanel = new JPanel();
        aiOptionsPanel.setLayout(new BoxLayout(aiOptionsPanel, BoxLayout.Y_AXIS));
        aiOptionsPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        humanStarts = radio("L'humain commence");
        aiStarts = radio("L'IA commence");
        humanStarts.setSelected(true);
        ButtonGroup sg = new ButtonGroup();
        sg.add(humanStarts);
        sg.add(aiStarts);
        aiOptionsPanel.add(humanStarts);
        aiOptionsPanel.add(Box.createVerticalStrut(8));
        aiOptionsPanel.add(aiStarts);
        JPanel aiWrapper = styledPanel("Options IA", aiOptionsPanel);
        inner.add(aiWrapper);
        inner.add(Box.createVerticalStrut(35));

        humanVsHuman.addActionListener(e -> aiWrapper.setVisible(false));
        humanVsAI.addActionListener(e -> aiWrapper.setVisible(true));

        // Bouton Commencer
        JButton startBtn = new RoundedButton("Commencer", ACCENT, BG);
        startBtn.setAlignmentX(CENTER_ALIGNMENT);
        startBtn.setMaximumSize(new Dimension(280, 50));
        startBtn.setPreferredSize(new Dimension(280, 50));
        startBtn.addActionListener(e -> {
            if (listener != null) {
                GameConfig config = new GameConfig(
                    (Integer) rowsSpinner.getValue(),
                    (Integer) colsSpinner.getValue(),
                    humanVsAI.isSelected(),
                    aiStarts.isSelected()
                );
                listener.onConfigConfirmed(config);
            }
        });
        inner.add(startBtn);

        add(inner);
    }

    public void setConfigListener(ConfigListener listener) {
        this.listener = listener;
    }

    private JPanel styledPanel(String title, JComponent content) {
        RoundedPanel p = new RoundedPanel(15, PANEL_BG);
        p.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tLbl.setForeground(ACCENT);
        header.add(tLbl, BorderLayout.WEST);
        
        p.add(header, BorderLayout.NORTH);
        
        content.setOpaque(false);
        p.add(content, BorderLayout.CENTER);
        
        p.setAlignmentX(CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(500, 250));
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(TXT_MAIN);
        return l;
    }

    private JSpinner spinner(int v, int mn, int mx) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, mn, mx, 1));
        s.setFont(new Font("Segoe UI", Font.BOLD, 16));
        s.setPreferredSize(new Dimension(80, 32));
        
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) s.getEditor();
        editor.getTextField().setBackground(BG);
        editor.getTextField().setForeground(TXT_MAIN);
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return s;
    }

    private JRadioButton radio(String t) {
        JRadioButton r = new JRadioButton(t);
        r.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        r.setForeground(TXT_MAIN);
        r.setBackground(PANEL_BG);
        r.setFocusPainted(false);
        r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return r;
    }

    private class RoundedPanel extends JPanel {
        private int radius;
        private Color bg;
        
        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(BORDER_C);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class RoundedButton extends JButton {
        private Color bgColor, fgColor;
        
        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setFont(new Font("Segoe UI", Font.BOLD, 18));
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(ACCENT_HOVER); }
                public void mouseExited(java.awt.event.MouseEvent e) { setBackground(bgColor); }
            });
        }
        
        @Override
        public void setBackground(Color bg) {
            this.bgColor = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(fgColor);
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }
}
