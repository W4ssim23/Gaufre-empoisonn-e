package model;

import java.util.*;

/**
 * Intelligence Artificielle pour la Gaufre Empoisonnée.
 * 
 * Utilise l'algorithme Minimax avec mémoïsation.
 * L'IA identifie tous les meilleurs coups (positions gagnantes) et en choisit
 * un aléatoirement pour ne pas être prévisible.
 */
public class AIPlayer {

    private final Map<String, Integer> memo; // Cache pour Minimax
    private final Random random;

    public AIPlayer() {
        this.memo = new HashMap<>();
        this.random = new Random();
    }

    /**
     * Trouve le meilleur coup pour le joueur courant.
     * Évalue tous les coups possibles, identifie les gagnants,
     * et en choisit un aléatoirement parmi les meilleurs.
     *
     * @param model L'état actuel du jeu
     * @return Le coup choisi
     */
    public Move findBestMove(GaufreModel model) {
        List<Move> validMoves = model.getValidMoves();
        if (validMoves.isEmpty()) {
            // Forcé de manger le poison
            return new Move(0, 0, model.getCurrentPlayer());
        }

        // Si un seul coup possible
        if (validMoves.size() == 1) {
            return validMoves.get(0);
        }

        memo.clear(); // Nettoyer le cache pour chaque recherche

        List<Move> winningMoves = new ArrayList<>();
        List<Move> allMoves = new ArrayList<>(validMoves);

        boolean[][] grid = model.copyGrid();
        int rows = model.getRows();
        int cols = model.getCols();

        for (Move move : allMoves) {
            // Simuler le coup
            boolean[][] newGrid = applyMoveToGrid(grid, move.getRow(), move.getCol(), rows, cols);

            // Évaluer la position résultante pour l'adversaire
            int score = minimax(newGrid, rows, cols, false);

            if (score > 0) {
                winningMoves.add(move);
            }
        }

        // Si des coups gagnants existent, en choisir un aléatoirement
        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        // Sinon, choisir un coup aléatoire (toutes les positions sont perdantes)
        // Stratégie : essayer de rendre le jeu le plus complexe possible
        return chooseBestLosingMove(allMoves, grid, rows, cols, model.getCurrentPlayer());
    }

    /**
     * Algorithme Minimax avec mémoïsation.
     * 
     * @param grid La grille actuelle
     * @param rows Nombre de lignes
     * @param cols Nombre de colonnes
     * @param isMaximizing true si c'est le tour du joueur IA
     * @return 1 si position gagnante pour le joueur courant, -1 sinon
     */
    private int minimax(boolean[][] grid, int rows, int cols, boolean isMaximizing) {
        String key = gridToKey(grid, rows, cols);

        if (memo.containsKey(key)) {
            int cached = memo.get(key);
            return isMaximizing ? cached : -cached;
        }

        // Vérifier si seul le poison reste
        if (isOnlyPoison(grid, rows, cols)) {
            // Le joueur courant est forcé de manger le poison → il perd
            int result = -1;
            memo.put(key, isMaximizing ? result : -result);
            return result;
        }

        List<int[]> moves = getValidMovesFromGrid(grid, rows, cols);

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int[] move : moves) {
                boolean[][] newGrid = applyMoveToGrid(grid, move[0], move[1], rows, cols);
                int score = minimax(newGrid, rows, cols, false);
                bestScore = Math.max(bestScore, score);
                if (bestScore == 1) break; // Élagage
            }
            memo.put(key, bestScore);
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int[] move : moves) {
                boolean[][] newGrid = applyMoveToGrid(grid, move[0], move[1], rows, cols);
                int score = minimax(newGrid, rows, cols, true);
                bestScore = Math.min(bestScore, score);
                if (bestScore == -1) break; // Élagage
            }
            memo.put(key, bestScore);
            return bestScore;
        }
    }

    /**
     * En position perdante, choisit le coup qui laisse le plus de cellules
     * (rend le jeu plus difficile pour l'adversaire humain).
     */
    private Move chooseBestLosingMove(List<Move> moves, boolean[][] grid, int rows, int cols, int player) {
        // Trier par nombre de cellules restantes (décroissant)
        // pour laisser le plus de complexité possible
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

    /**
     * Applique un coup sur une copie de la grille.
     */
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

    /**
     * Retourne les coups valides depuis une grille (hors poison sauf si unique).
     */
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

    /**
     * Génère une clé unique pour la position (pour la mémoïsation).
     * Utilise le profil de colonnes (nombre de cellules par ligne) pour représenter la position.
     */
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
