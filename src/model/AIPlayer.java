package model;

import java.util.*;

// uses mini-max and memoisation
public class AIPlayer {

    private final Map<String, Integer> memo; // Cache pour Minimax
    private final Random random;

    public AIPlayer() {
        this.memo = new HashMap<>();
        this.random = new Random();
    }

    // find all good moves and select a random one of them
    public Move findBestMove(GaufreModel model) {
        List<Move> validMoves = model.getValidMoves();
        if (validMoves.isEmpty()) {
            // force eat poison
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
            // Simuler le coup
            boolean[][] newGrid = applyMoveToGrid(grid, move.getRow(), move.getCol(), rows, cols);

            // Évaluer la position résultante pour l'adversaire
            int score = minimax(newGrid, rows, cols);

            if (score == -1) { // si l’adversaire perd → coup gagnant 
                winningMoves.add(move);
            }
        }

        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        return chooseBestLosingMove(allMoves, grid, rows, cols, model.getCurrentPlayer());
    }


    private int minimax(boolean[][] grid, int rows, int cols) {

        String key = gridToKey(grid, rows, cols);

        // an already treated state
        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        // terminal position : only poison left => current player loses
        if (isOnlyPoison(grid, rows, cols)) {
            memo.put(key, -1);
            return -1;
        }

        List<int[]> moves = getValidMovesFromGrid(grid, rows, cols);

        // try all moves if ANY move leads to a losing position it's winning
        for (int[] move : moves) {
            boolean[][] newGrid = applyMoveToGrid(grid, move[0], move[1], rows, cols);

            int result = minimax(newGrid, rows, cols);

            if (result == -1) {
                memo.put(key, 1); // winning state
                return 1;
            }
        }

        // If no winning move exists, this is losing
        memo.put(key, -1);
        return -1;
    }


    //if there is no wining moves, just make the game longer so we can increase the chance of the player fambling
    private Move chooseBestLosingMove(List<Move> moves, boolean[][] grid, int rows, int cols, int player) {
        List<Move> bestMoves = new ArrayList<>();
        int maxCells = -1;

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


    // Génère une clé unique pour la position (pour la mémoïsation)
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


    /**
     * Compte le nombre de cellules restantes.
     */
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