package view;

import model.GameConfig;
import model.GaufreModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class GaufreView extends JFrame implements GaufreModel.ModelListener {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ConfigPanel configPanel;
    private JPanel gamePanel;

    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel moveCountLabel;
    private JButton undoBtn, redoBtn, newGameBtn, saveBtn, loadBtn;
    private GaufreModel model;
    private GameConfig config;

    // Catppuccin Macchiato Palette
    private static final Color BG = new Color(30, 30, 46);
    private static final Color PANEL_BG = new Color(49, 50, 68);
    private static final Color ACCENT = new Color(249, 226, 175);
    private static final Color TXT = new Color(205, 214, 244);
    private static final Color P1_COLOR = new Color(137, 180, 250); // Blue
    private static final Color P2_COLOR = new Color(243, 139, 168); // Red
    private static final Color BTN_BLUE = new Color(137, 180, 250);
    private static final Color BTN_GREEN = new Color(166, 227, 161);

    private static final String CARD_CONFIG = "config";
    private static final String CARD_GAME = "game";

    public GaufreView() {
        super("Gaufre Empoisonnée");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setBackground(BG);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        configPanel = new ConfigPanel();
        cardPanel.add(configPanel, CARD_CONFIG);

        gamePanel = buildGamePanel();
        cardPanel.add(gamePanel, CARD_GAME);

        setContentPane(cardPanel);
        showConfig();
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLbl = new JLabel("Gaufre Empoisonnée");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setForeground(ACCENT);
        header.add(titleLbl, BorderLayout.WEST);

        statusLabel = new JLabel("Tour du Joueur 1");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(P1_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(statusLabel, BorderLayout.EAST);

        // Add header shadow/border
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(header, BorderLayout.CENTER);
        JPanel shadow = new JPanel();
        shadow.setBackground(new Color(24, 24, 37)); // Mantle shadow
        shadow.setPreferredSize(new Dimension(1, 3));
        headerContainer.add(shadow, BorderLayout.SOUTH);

        panel.add(headerContainer, BorderLayout.NORTH);

        boardPanel = new BoardPanel(null);
        panel.add(boardPanel, BorderLayout.CENTER);

        // Footer Shadow
        JPanel footerShadow = new JPanel();
        footerShadow.setBackground(new Color(24, 24, 37));
        footerShadow.setPreferredSize(new Dimension(1, 3));

        JPanel controlBar = new JPanel(new GridBagLayout());
        controlBar.setBackground(PANEL_BG);
        controlBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        undoBtn = makeBtn("Annuler", BTN_BLUE, BG);
        redoBtn = makeBtn("Refaire", BTN_BLUE, BG);

        saveBtn = makeBtn("Sauvegarder", BTN_GREEN, BG);
        loadBtn = makeBtn("Charger", BTN_GREEN, BG);

        newGameBtn = makeBtn("Nouvelle Partie", ACCENT, BG);

        moveCountLabel = new JLabel("Coups: 0");
        moveCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        moveCountLabel.setForeground(TXT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        Insets normalInsets = new Insets(0, 8, 0, 8);
        Insets sepInsets = new Insets(0, 25, 0, 25);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlBar.add(Box.createHorizontalGlue(), gbc);
        
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx++;

        gbc.insets = normalInsets;
        controlBar.add(undoBtn, gbc);
        gbc.gridx++;
        controlBar.add(redoBtn, gbc);
        gbc.gridx++;

        gbc.insets = sepInsets;
        controlBar.add(makeVerticalSeparator(), gbc);
        gbc.gridx++;

        gbc.insets = normalInsets;
        controlBar.add(saveBtn, gbc);
        gbc.gridx++;
        controlBar.add(loadBtn, gbc);
        gbc.gridx++;

        gbc.insets = sepInsets;
        controlBar.add(makeVerticalSeparator(), gbc);
        gbc.gridx++;

        gbc.insets = normalInsets;
        controlBar.add(moveCountLabel, gbc);
        gbc.gridx++;

        gbc.insets = sepInsets;
        controlBar.add(makeVerticalSeparator(), gbc);
        gbc.gridx++;

        gbc.insets = normalInsets;
        controlBar.add(newGameBtn, gbc);
        
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlBar.add(Box.createHorizontalGlue(), gbc);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
        bottomPanel.setBackground(PANEL_BG);
        bottomPanel.add(footerShadow, BorderLayout.NORTH);
        bottomPanel.add(controlBar, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JSeparator makeVerticalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(new Color(69, 71, 90)); // Surface1
        sep.setBackground(PANEL_BG);
        sep.setMaximumSize(new Dimension(2, 32));
        sep.setPreferredSize(new Dimension(2, 32));
        return sep;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        return new RoundedButton(text, bg, fg);
    }

    // Custom Rounded Button Class
    private class RoundedButton extends JButton {
        private Color bgColor, fgColor;
        
        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Padding
            setPreferredSize(new Dimension(getPreferredSize().width + 30, 42));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { 
                    setBackground(bgColor.brighter()); 
                }
                public void mouseExited(java.awt.event.MouseEvent e) { 
                    setBackground(bgColor); 
                }
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

    public void showConfig() {
        cardLayout.show(cardPanel, CARD_CONFIG);
    }

    public void showGame() {
        cardLayout.show(cardPanel, CARD_GAME);
    }

    public ConfigPanel getConfigPanel() { return configPanel; }
    public BoardPanel getBoardPanel() { return boardPanel; }
    public JButton getUndoBtn() { return undoBtn; }
    public JButton getRedoBtn() { return redoBtn; }
    public JButton getNewGameBtn() { return newGameBtn; }
    public JButton getSaveBtn() { return saveBtn; }
    public JButton getLoadBtn() { return loadBtn; }

    public void updateModel(GaufreModel newModel, GameConfig newConfig) {
        if (this.model != null) this.model.removeListener(this);
        this.model = newModel;
        this.config = newConfig;
        newModel.addListener(this);
        boardPanel.setModel(newModel);
        onModelChanged();
    }

    public void setMoveCount(int count) {
        moveCountLabel.setText("Coups: " + count);
    }

    public void setAIThinkingState(boolean thinking) {
        if (thinking) {
            statusLabel.setText("L'IA est en train de réfléchir...");
            statusLabel.setForeground(ACCENT);
        } else {
            onModelChanged();
        }
    }

    public void showGameOver(int loser) {
        String winner;
        String boardMsg;
        if (config.isVsAI()) {
            if (loser == config.getAiPlayer()) {
                winner = "Vous avez gagné !";
                boardMsg = "Tu as gagné !";
            } else {
                winner = "L'IA a gagné !";
                boardMsg = "Tu as perdu !";
            }
        } else {
            winner = "Joueur " + (loser == 1 ? "2" : "1") + " a gagné !";
            boardMsg = winner;
        }
        statusLabel.setText("Fin - " + winner);
        statusLabel.setForeground(ACCENT);
        
        boardPanel.setGameOverMessage(boardMsg);
    }

    @Override
    public void onModelChanged() {
        if (model != null && !model.isGameOver()) {
            int cp = model.getCurrentPlayer();
            String name;
            if (config != null && config.isVsAI()) {
                name = (cp == config.getAiPlayer()) ? "IA" : "Humain";
            } else {
                name = "Joueur " + cp;
            }
            statusLabel.setText("Tour: " + name + " (J" + cp + ")");
            statusLabel.setForeground(cp == 1 ? P1_COLOR : P2_COLOR);
            boardPanel.setGameOverMessage(null);
        }
        this.boardPanel.repaint();
    }

    @Override
    public void onGameOver(int loser) {
        SwingUtilities.invokeLater(() -> showGameOver(loser));
    }
}
