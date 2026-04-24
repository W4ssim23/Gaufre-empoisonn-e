import controller.GaufreController;
import view.*;

import javax.swing.*;

/**
 * Point d'entrée du jeu de la Gaufre Empoisonnée (Chomp).
 * Crée une seule fenêtre qui affiche d'abord la configuration,
 * puis bascule vers le jeu sans fermeture/ouverture.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            GaufreView view = new GaufreView();
            GaufreController controller = new GaufreController(view);
            view.setVisible(true);
        });
    }
}
