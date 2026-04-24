package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la persistance du jeu (sauvegarde et chargement).
 * Utilise la sérialisation Java pour sauvegarder l'état complet
 * du plateau ET l'historique des coups (Undo/Redo).
 */
public class GamePersistence {

    public static class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean[][] grid;
        public int rows;
        public int cols;
        public int currentPlayer;
        public boolean gameOver;
        public int loser;
        public GameConfig config;
        public List<MoveCommand> undoStack;
        public List<MoveCommand> redoStack;

        public SaveData() {
            undoStack = new ArrayList<>();
            redoStack = new ArrayList<>();
        }
    }

    public static void save(File file, GaufreModel model, GameHistory history, GameConfig config) throws IOException {
        SaveData data = new SaveData();
        data.grid = model.copyGrid();
        data.rows = model.getRows();
        data.cols = model.getCols();
        data.currentPlayer = model.getCurrentPlayer();
        data.gameOver = model.isGameOver();
        data.loser = model.getLoser();
        data.config = config;
        data.undoStack = history.getUndoCommands();
        data.redoStack = history.getRedoCommands();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        }
    }

    /**
     * Charge l'état complet du jeu depuis un fichier.
     */
    public static SaveData load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (SaveData) ois.readObject();
        }
    }
}
