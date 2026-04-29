package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GaufreModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean[][] grid; // true = case présente, false = mangée
    private int rows;
    private int cols;
    private int currentPlayer; // 1 ou 2
    private boolean gameOver;
    private int loser; // 0 = pas fini, 1 ou 2 = perdant

    private transient List<ModelListener> listeners;

    // interface for the observers (listeners)
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

    public void reset() {
        this.grid = new boolean[rows][cols];
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

    //doesnt check if move availble !
    public void applyMove(Move move) {
        int r = move.getRow();
        int c = move.getCol();

        if (r == 0 && c == 0) {
            gameOver = true;
            loser = move.getPlayer();
        }

        for (int i = r; i < rows; i++) {
            for (int j = c; j < cols; j++) {
                grid[i][j] = false;
            }
        }

        currentPlayer = (move.getPlayer() == 1) ? 2 : 1;

        fireModelChanged();
        if (gameOver) {
            fireGameOver(loser);
        }
    }

    // for undo
    public void restoreState(boolean[][] savedGrid, int player, boolean wasGameOver, int wasLoser) {
        for (int i = 0; i < rows; i++) {
            System.arraycopy(savedGrid[i], 0, grid[i], 0, cols);
        }
        this.currentPlayer = player;
        this.gameOver = wasGameOver;
        this.loser = wasLoser;
        fireModelChanged();
    }


    public boolean isValidMove(int row, int col) {
        if (gameOver) return false;
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        return grid[row][col];
    }


    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>();
        if (gameOver) return moves;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j]) {
                    if (i == 0 && j == 0) continue;
                    moves.add(new Move(i, j, currentPlayer));
                }
            }
        }
        if (moves.isEmpty() && grid[0][0]) {
            moves.add(new Move(0, 0, currentPlayer));
        }
        return moves;
    }


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


    public boolean[][] copyGrid() {
        boolean[][] copy = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }


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
