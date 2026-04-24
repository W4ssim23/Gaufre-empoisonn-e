package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Gère l'historique complet des coups avec deux piles (Undo/Redo).
 * Permet d'annuler et de refaire des coups sans limite.
 */
public class GameHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Stack<MoveCommand> undoStack;
    private final Stack<MoveCommand> redoStack;

    public GameHistory() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
    }

    /**
     * Exécute une commande et l'ajoute à l'historique.
     * Vide la pile Redo (un nouveau coup invalide les coups refaits).
     */
    public void executeCommand(MoveCommand command, GaufreModel model) {
        command.execute(model);
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Annule le dernier coup.
     * @return true si un coup a été annulé, false si l'historique est vide.
     */
    public boolean undo(GaufreModel model) {
        if (undoStack.isEmpty()) {
            return false;
        }
        MoveCommand command = undoStack.pop();
        command.undo(model);
        redoStack.push(command);
        return true;
    }

    /**
     * Refait le dernier coup annulé.
     * @return true si un coup a été refait, false si pas de coup à refaire.
     */
    public boolean redo(GaufreModel model) {
        if (redoStack.isEmpty()) {
            return false;
        }
        MoveCommand command = redoStack.pop();
        command.execute(model);
        undoStack.push(command);
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Réinitialise l'historique complet.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * @return Liste ordonnée de tous les coups effectués (pour la sauvegarde).
     */
    public List<MoveCommand> getUndoCommands() {
        return new ArrayList<>(undoStack);
    }

    /**
     * @return Liste ordonnée de tous les coups annulés (pour la sauvegarde).
     */
    public List<MoveCommand> getRedoCommands() {
        return new ArrayList<>(redoStack);
    }

    /**
     * Restaure l'historique depuis des listes sauvegardées.
     */
    public void restore(List<MoveCommand> undoCommands, List<MoveCommand> redoCommands) {
        undoStack.clear();
        redoStack.clear();
        undoStack.addAll(undoCommands);
        redoStack.addAll(redoCommands);
    }

    public int getMoveCount() {
        return undoStack.size();
    }
}
