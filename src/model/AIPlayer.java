package model;

import java.util.*;

/**
 * Utilise l'algorithme Minimax avec mémoïsation.
 * L'IA identifie tous les meilleurs coups (positions gagnantes) et en choisit
 * un aléatoirement pour ne pas être prévisible.
 */
public class AIPlayer {

    private final Map<String, Integer> memo;
    private final Random random;

    public AIPlayer() {
        this.memo = new HashMap<>();
        this.random = new Random();
    }

    public Move findBestMove(GaufreModel model) {
        List<Move> validMoves = model.getValidMoves();
        if (validMoves.isEmpty()) {
            return new Move(0, 0, model.getCurrentPlayer());
        }

        if (validMoves.size() == 1) {
            return validMoves.get(0);
        }

        memo.clear();

        List<Move> winningMoves = new ArrayList<>();
        List<Move> allMoves = new ArrayList<>(validMoves);

        boolean[][] grid = model.copyGrid();
        int rows = model.getRows();
        int cols = model.getCols();

        for (Move move : allMoves) {
            boolean[][] newGrid = applyMoveToGrid(grid, move.getRow(), move.getCol(), rows, cols);

            int score = minimax(newGrid, rows, cols);
            if (score == -1) {
                winningMoves.add(move);
            }
        }

        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }
        return chooseBestLosingMove(allMoves, grid, rows, cols, model.getCurrentPlayer());
    }

    /**
     * Algorithme Minimax avec mémoïsation pour un jeu impartial.
     * 
     * @param grid La grille actuelle
     * @param rows Nombre de lignes
     * @param cols Nombre de colonnes
     * @return 1 si position gagnante pour le joueur courant, -1 si position perdante
     */
    private int minimax(boolean[][] grid, int rows, int cols) {
        String key = gridToKey(grid, rows, cols);

        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        if (isOnlyPoison(grid, rows, cols)) {
            memo.put(key, -1);
            return -1;
        }

        List<int[]> moves = getValidMovesFromGrid(grid, rows, cols);

        for (int[] move : moves) {
            boolean[][] newGrid = applyMoveToGrid(grid, move[0], move[1], rows, cols);
            int score = minimax(newGrid, rows, cols);
            
            if (score == -1) {
                memo.put(key, 1);
                return 1;
            }
        }

        memo.put(key, -1);
        return -1;
    }

    private Move chooseBestLosingMove(List<Move> moves, boolean[][] grid, int rows, int cols, int player) {
        Move bestMove = null;
        int maxCells = -1;

        List<Move> bestMoves = new ArrayList<>();

        for (Move move : moves) {
            boolean[][] newGrid = applyMoveToGrid(grid, move.getRow(), move.getCol(), rows, cols);
            int cellCount = countCells(newGrid, rows, cols);
            if (cellCount > maxCells) {
                maxCells = cellCount;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (cellCount == maxCells) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private boolean[][] applyMoveToGrid(boolean[][] grid, int row, int col, int rows, int cols) {
        boolean[][] newGrid = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, cols);
        }
        for (int i = row; i < rows; i++) {
            for (int j = col; j < cols; j++) {
                newGrid[i][j] = false;
            }
        }
        return newGrid;
    }

    /**
     * Vérifie si seule la case (0,0) reste.
     */
    private boolean isOnlyPoison(boolean[][] grid, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] && !(i == 0 && j == 0)) {
                    return false;
                }
            }
        }
        return grid[0][0];
    }

    private List<int[]> getValidMovesFromGrid(boolean[][] grid, int rows, int cols) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] && !(i == 0 && j == 0)) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        if (moves.isEmpty() && grid[0][0]) {
            moves.add(new int[]{0, 0});
        }
        return moves;
    }

    private String gridToKey(boolean[][] grid, int rows, int cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            int count = 0;
            for (int j = 0; j < cols; j++) {
                if (grid[i][j]) count++;
            }
            sb.append(count);
            if (i < rows - 1) sb.append(',');
        }
        return sb.toString();
    }

    private int countCells(boolean[][] grid, int rows, int cols) {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j]) count++;
            }
        }
        return count;
    }
}
