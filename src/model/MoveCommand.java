package model;

import java.io.Serializable;

/**
 * Implémentation du pattern Command pour un coup dans la Gaufre.
 * Enregistre l'état du plateau avant le coup pour permettre l'annulation (Undo).
 */
public class MoveCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Move move;
    private final boolean[][] previousGridState; 
    private final int previousPlayer;
    private final boolean previousGameOver;
    private final int previousLoser;

    public MoveCommand(Move move, GaufreModel model) {
        this.move = move;
        this.previousGridState = model.copyGrid();
        this.previousPlayer = model.getCurrentPlayer();
        this.previousGameOver = model.isGameOver();
        this.previousLoser = model.getLoser();
    }

    public void execute(GaufreModel model) {
        model.applyMove(move);
    }

    public void undo(GaufreModel model) {
        model.restoreState(previousGridState, previousPlayer, previousGameOver, previousLoser);
    }

    public Move getMove() {
        return move;
    }

    public boolean[][] getPreviousGridState() {
        return previousGridState;
    }

    public int getPreviousPlayer() {
        return previousPlayer;
    }

    @Override
    public String toString() {
        return "Command[" + move + "]";
    }
}
