package model;

import java.io.Serializable;

/**
 * Représente un coup joué dans le jeu de la Gaufre.
 */
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;
    private final int player; 

    public Move(int row, int col, int player) {
        this.row = row;
        this.col = col;
        this.player = player;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Joueur " + player + " → (" + row + ", " + col + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return row == move.row && col == move.col && player == move.player;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * row + col) + player;
    }
}
