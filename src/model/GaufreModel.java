package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle principal de la Gaufre Empoisonnée (Chomp).
 * 
 * La gaufre est une grille N×M. La case (0,0) est empoisonnée (poison).
 * Lorsqu'un joueur clique sur (r,c), toutes les cases (i,j) avec i≥r et j≥c sont mangées.
 * Le joueur qui mange la case (0,0) a perdu.
 */
public class GaufreModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean[][] grid; // true = case présente, false = mangée
    private int rows;
    private int cols;
    private int currentPlayer; // 1 ou 2
    private boolean gameOver;
    private int loser; // 0 = pas fini, 1 ou 2 = perdant

    private transient List<ModelListener> listeners;

    public interface ModelListener {
        void onModelChanged();
        void onGameOver(int loser);
    }

    public GaufreModel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.listeners = new ArrayList<>();
        reset();
    }

    /**
     * Réinitialise le plateau à son état initial.
     */
    public void reset() {
        grid = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = true;
            }
        }
        currentPlayer = 1;
        gameOver = false;
        loser = 0;
        fireModelChanged();
    }

    /**
     * Applique un coup : mange toutes les cases (i,j) avec i≥row et j≥col.
     * Ne vérifie PAS la validité - utilisé en interne par MoveCommand.
     */
    public void applyMove(Move move) {
        int r = move.getRow();
        int c = move.getCol();

        // Si on mange (0,0), le joueur courant perd
        if (r == 0 && c == 0) {
            gameOver = true;
            loser = move.getPlayer();
        }

        // Manger toutes les cases (i,j) avec i ≥ r et j ≥ c
        for (int i = r; i < rows; i++) {
            for (int j = c; j < cols; j++) {
                grid[i][j] = false;
            }
        }

        // Changer de joueur
        currentPlayer = (move.getPlayer() == 1) ? 2 : 1;

        fireModelChanged();
        if (gameOver) {
            fireGameOver(loser);
        }
    }

    /**
     * Restaure un état complet (utilisé par Undo).
     */
    public void restoreState(boolean[][] savedGrid, int player, boolean wasGameOver, int wasLoser) {
        for (int i = 0; i < rows; i++) {
            System.arraycopy(savedGrid[i], 0, grid[i], 0, cols);
        }
        this.currentPlayer = player;
        this.gameOver = wasGameOver;
        this.loser = wasLoser;
        fireModelChanged();
    }

    /**
     * Vérifie si un coup est valide.
     */
    public boolean isValidMove(int row, int col) {
        if (gameOver) return false;
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        return grid[row][col]; // La case doit être encore présente
    }

    /**
     * Retourne tous les coups valides.
     */
    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>();
        if (gameOver) return moves;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j]) {
                    // Ne pas inclure (0,0) sauf si c'est le seul coup possible
                    if (i == 0 && j == 0) continue;
                    moves.add(new Move(i, j, currentPlayer));
                }
            }
        }
        // Si aucun coup hors (0,0), il faut manger le poison
        if (moves.isEmpty() && grid[0][0]) {
            moves.add(new Move(0, 0, currentPlayer));
        }
        return moves;
    }

    /**
     * Vérifie si seule la case empoisonnée (0,0) reste.
     */
    public boolean isOnlyPoisonLeft() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] && !(i == 0 && j == 0)) {
                    return false;
                }
            }
        }
        return grid[0][0];
    }

    /**
     * Crée une copie profonde de la grille.
     */
    public boolean[][] copyGrid() {
        boolean[][] copy = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    /**
     * Vérifie si une case est présente.
     */
    public boolean isCellPresent(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        return grid[row][col];
    }

    public void addListener(ModelListener listener) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        if (listeners != null) listeners.remove(listener);
    }

    private void fireModelChanged() {
        if (listeners == null) return;
        for (ModelListener l : listeners) {
            l.onModelChanged();
        }
    }

    private void fireGameOver(int loser) {
        if (listeners == null) return;
        for (ModelListener l : listeners) {
            l.onGameOver(loser);
        }
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getLoser() {
        return loser;
    }

    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
    }
}
