package view;

import model.GameConfig;
import model.GaufreModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Vue principale du jeu de la Gaufre Empoisonnée.
 * Utilise un CardLayout pour basculer entre le panneau de configuration
 * et le panneau de jeu dans la même fenêtre, sans fermeture/ouverture.
 */
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

    private static final Color BG = new Color(35, 35, 45);
    private static final Color PANEL_BG = new Color(45, 45, 58);
    private static final Color ACCENT = new Color(235, 195, 80);
    private static final Color TXT = new Color(220, 220, 235);
    private static final Color P1_COLOR = new Color(100, 180, 255);
    private static final Color P2_COLOR = new Color(255, 130, 100);

    private static final String CARD_CONFIG = "config";
    private static final String CARD_GAME = "game";

    public GaufreView() {
        super("Gaufre Empoisonnee - Chomp");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
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
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLbl = new JLabel("Gaufre Empoisonnee");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(ACCENT);
        header.add(titleLbl, BorderLayout.WEST);

        statusLabel = new JLabel("Tour du Joueur 1");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(P1_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(statusLabel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        boardPanel = new BoardPanel(null);
        boardPanel.setBackground(new Color(250, 245, 235));
        panel.add(boardPanel, BorderLayout.CENTER);

        // Separator + Footer
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(80, 80, 100));
        separator.setBackground(PANEL_BG);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_BG);
        footer.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftBtns.setBackground(PANEL_BG);
        undoBtn = makeBtn("Annuler", new Color(100, 160, 220));
        redoBtn = makeBtn("Refaire", new Color(100, 160, 220));
        leftBtns.add(undoBtn);
        leftBtns.add(redoBtn);

        JPanel centerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerInfo.setBackground(PANEL_BG);
        moveCountLabel = new JLabel("Coups: 0");
        moveCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        moveCountLabel.setForeground(new Color(150, 150, 170));
        centerInfo.add(moveCountLabel);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.setBackground(PANEL_BG);
        saveBtn = makeBtn("Sauvegarder", new Color(80, 170, 120));
        loadBtn = makeBtn("Charger", new Color(80, 170, 120));
        newGameBtn = makeBtn("Nouvelle Partie", ACCENT);
        newGameBtn.setForeground(BG);
        rightBtns.add(saveBtn);
        rightBtns.add(loadBtn);
        rightBtns.add(newGameBtn);

        footer.add(leftBtns, BorderLayout.WEST);
        footer.add(centerInfo, BorderLayout.CENTER);
        footer.add(rightBtns, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
        bottomPanel.setBackground(PANEL_BG);
        bottomPanel.add(separator, BorderLayout.NORTH);
        bottomPanel.add(footer, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(TXT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6, 14, 6, 14));
        Color hover = bg.brighter();
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // === Navigation entre les cartes ===

    public void showConfig() {
        cardLayout.show(cardPanel, CARD_CONFIG);
    }

    public void showGame() {
        cardLayout.show(cardPanel, CARD_GAME);
    }

    // === Accesseurs ===

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

    public void showGameOver(int loser) {
        String winner;
        if (config.isVsAI()) {
            winner = (loser == config.getAiPlayer()) ? "Vous avez gagne !" : "L'IA a gagne !";
        } else {
            winner = "Joueur " + (loser == 1 ? "2" : "1") + " a gagne !";
        }
        statusLabel.setText("Fin - " + winner);
        statusLabel.setForeground(ACCENT);

        JOptionPane.showMessageDialog(this,
            "Le Joueur " + loser + " a mange le poison !\n" + winner,
            "Fin de Partie", JOptionPane.INFORMATION_MESSAGE);
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
        }
        boardPanel.repaint();
    }

    @Override
    public void onGameOver(int loser) {
        SwingUtilities.invokeLater(() -> showGameOver(loser));
    }
}
