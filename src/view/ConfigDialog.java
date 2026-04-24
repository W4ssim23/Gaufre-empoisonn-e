package view;

import model.GameConfig;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ConfigDialog extends JDialog {
    private JSpinner rowsSpinner, colsSpinner;
    private JRadioButton humanVsHuman, humanVsAI, humanStarts, aiStarts;
    private JPanel aiOptionsPanel;
    private boolean confirmed = false;
    private GameConfig config;

    private static final Color BG = new Color(45,45,55);
    private static final Color PANEL_BG = new Color(55,55,70);
    private static final Color ACCENT = new Color(235,195,80);
    private static final Color TXT = new Color(230,230,240);

    public ConfigDialog(JFrame parent) {
        super(parent, "Nouvelle Partie", true);
        setResizable(false);
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20,25,20,25));

        JLabel title = new JLabel("Gaufre Empoisonnee");
        title.setFont(new Font("Segoe UI",Font.BOLD,20));
        title.setForeground(ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);
        main.add(Box.createVerticalStrut(15));

        // Taille
        JPanel sizeP = styledPanel("Taille de la grille");
        sizeP.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,8,4,8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx=0; g.gridy=0;
        sizeP.add(lbl("Lignes (N):"),g);
        g.gridx=1;
        rowsSpinner = spinner(5,2,10);
        sizeP.add(rowsSpinner,g);
        g.gridx=0; g.gridy=1;
        sizeP.add(lbl("Colonnes (M):"),g);
        g.gridx=1;
        colsSpinner = spinner(7,2,12);
        sizeP.add(colsSpinner,g);
        main.add(sizeP);
        main.add(Box.createVerticalStrut(10));

        // Mode
        JPanel modeP = styledPanel("Mode de jeu");
        modeP.setLayout(new BoxLayout(modeP, BoxLayout.Y_AXIS));
        humanVsHuman = radio("Humain vs Humain");
        humanVsAI = radio("Humain vs IA");
        humanVsAI.setSelected(true);
        ButtonGroup mg = new ButtonGroup();
        mg.add(humanVsHuman); mg.add(humanVsAI);
        modeP.add(humanVsHuman);
        modeP.add(Box.createVerticalStrut(4));
        modeP.add(humanVsAI);
        main.add(modeP);
        main.add(Box.createVerticalStrut(10));

        // AI opts
        aiOptionsPanel = styledPanel("Options IA");
        aiOptionsPanel.setLayout(new BoxLayout(aiOptionsPanel, BoxLayout.Y_AXIS));
        humanStarts = radio("L'humain commence");
        aiStarts = radio("L'IA commence");
        humanStarts.setSelected(true);
        ButtonGroup sg = new ButtonGroup();
        sg.add(humanStarts); sg.add(aiStarts);
        aiOptionsPanel.add(humanStarts);
        aiOptionsPanel.add(Box.createVerticalStrut(4));
        aiOptionsPanel.add(aiStarts);
        main.add(aiOptionsPanel);
        main.add(Box.createVerticalStrut(18));

        humanVsHuman.addActionListener(e -> aiOptionsPanel.setVisible(false));
        humanVsAI.addActionListener(e -> aiOptionsPanel.setVisible(true));

        // Buttons
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER,12,0));
        bp.setBackground(BG);
        JButton cancel = btn("Annuler", new Color(120,120,135));
        cancel.addActionListener(e -> { confirmed=false; dispose(); });
        JButton start = btn("Commencer", ACCENT);
        start.setForeground(BG);
        start.addActionListener(e -> {
            confirmed = true;
            config = new GameConfig((Integer)rowsSpinner.getValue(),
                (Integer)colsSpinner.getValue(), humanVsAI.isSelected(), aiStarts.isSelected());
            dispose();
        });
        bp.add(cancel); bp.add(start);
        main.add(bp);

        setContentPane(main);
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel styledPanel(String t) {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        TitledBorder b = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80,80,100),1,true), t);
        b.setTitleFont(new Font("Segoe UI",Font.BOLD,13));
        b.setTitleColor(ACCENT);
        p.setBorder(BorderFactory.createCompoundBorder(b, new EmptyBorder(8,8,8,8)));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(350,120));
        return p;
    }
    private JLabel lbl(String t) { JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,14)); l.setForeground(TXT); return l; }
    private JSpinner spinner(int v,int mn,int mx) { JSpinner s=new JSpinner(new SpinnerNumberModel(v,mn,mx,1)); s.setFont(new Font("Segoe UI",Font.BOLD,14)); s.setPreferredSize(new Dimension(70,28)); return s; }
    private JRadioButton radio(String t) { JRadioButton r=new JRadioButton(t); r.setFont(new Font("Segoe UI",Font.PLAIN,14)); r.setForeground(TXT); r.setBackground(PANEL_BG); r.setFocusPainted(false); return r; }
    private JButton btn(String t, Color bg) {
        JButton b=new JButton(t); b.setFont(new Font("Segoe UI",Font.BOLD,14)); b.setBackground(bg); b.setForeground(TXT);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setPreferredSize(new Dimension(140,36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color h=bg.brighter();
        b.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){b.setBackground(h);}
            public void mouseExited(java.awt.event.MouseEvent e){b.setBackground(bg);}
        });
        return b;
    }
    public boolean isConfirmed(){return confirmed;}
    public GameConfig getConfig(){return config;}
}
