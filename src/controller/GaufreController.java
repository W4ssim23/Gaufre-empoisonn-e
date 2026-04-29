package controller;

import model.*;
import view.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;




public class GaufreController {

    private GaufreModel model;
    private GaufreView view;
    private GameHistory history;
    private GameConfig config;
    private AIPlayer aiPlayer;
    private boolean aiThinking = false;

    public GaufreController(GaufreView view) {
        this.view = view;
        this.aiPlayer = new AIPlayer();

        view.getConfigPanel().setConfigListener(this::onConfigConfirmed);
        view.getNewGameBtn().addActionListener(e -> handleNewGame());
        view.getUndoBtn().addActionListener(e -> handleUndo());
        view.getRedoBtn().addActionListener(e -> handleRedo());
        view.getSaveBtn().addActionListener(e -> handleSave());
        view.getLoadBtn().addActionListener(e -> handleLoad());
    }

    private void onConfigConfirmed(GameConfig newConfig) {
        this.config = newConfig;
        this.model = new GaufreModel(newConfig.getRows(), newConfig.getCols());
        this.history = new GameHistory();

        view.updateModel(model, config);
        view.getBoardPanel().setCellClickListener(this::handleCellClick);
        updateButtonStates();

        view.showGame();

        if (config.isVsAI() && config.isAiStarts()) {
            SwingUtilities.invokeLater(this::playAIMove);
        }
    }

    private void handleCellClick(int row, int col) {
        if (model.isGameOver() || aiThinking) return;
        if (view.getBoardPanel().isAnimating()) return;
        if (config.isVsAI() && model.getCurrentPlayer() == config.getAiPlayer()) return;
        if (!model.isValidMove(row, col)) return;

        playMove(row, col);

        if (!model.isGameOver() && config.isVsAI()
                && model.getCurrentPlayer() == config.getAiPlayer()) {
            SwingUtilities.invokeLater(this::playAIMove);
        }
    }

    private void playMove(int row, int col) {
        boolean[][] gridBefore = model.copyGrid();
        view.getBoardPanel().triggerDestructionAnimation(row, col, gridBefore);

        Move move = new Move(row, col, model.getCurrentPlayer());
        MoveCommand command = new MoveCommand(move, model);
        history.executeCommand(command, model);
        updateButtonStates();
    }

    private void playAIMove() {
        if (model.isGameOver()) return;
        aiThinking = true;
        view.setAIThinkingState(true);
        view.getBoardPanel().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                try {
                    // Attente artificielle 
                    long delay = 2000 + (long)(Math.random() * 2000);
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {}
                return aiPlayer.findBestMove(model);
            }

            @Override
            protected void done() {
                try {
                    Move move = get();
                    if (move != null && !model.isGameOver()) {
                        playMove(move.getRow(), move.getCol());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    aiThinking = false;
                    view.setAIThinkingState(false);
                    view.getBoardPanel().setCursor(java.awt.Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    private void handleUndo() {
        if (aiThinking) return;
        if (!history.canUndo()) return;

        if (config.isVsAI()) {
            history.undo(model);
            if (history.canUndo()) {
                history.undo(model);
            }
        } else {
            history.undo(model);
        }
        updateButtonStates();
    }

    private void handleRedo() {
        if (aiThinking) return;
        if (!history.canRedo()) return;

        if (config.isVsAI()) {
            history.redo(model);
            if (history.canRedo()) {
                history.redo(model);
            }
        } else {
            history.redo(model);
        }
        updateButtonStates();
    }

    private void handleNewGame() {
        if (aiThinking) return;
        view.showConfig();
    }

    private void handleSave() {
        if (aiThinking) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sauvegarder la partie");
        chooser.setFileFilter(new FileNameExtensionFilter("Gaufre Save (*.gaufre)", "gaufre"));
        chooser.setSelectedFile(new File("partie.gaufre"));

        if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".gaufre")) {
                file = new File(file.getAbsolutePath() + ".gaufre");
            }
            try {
                GamePersistence.save(file, model, history, config);
                JOptionPane.showMessageDialog(view,
                    "Partie sauvegardee avec succes !",
                    "Sauvegarde", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view,
                    "Erreur lors de la sauvegarde :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLoad() {
        if (aiThinking) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Charger une partie");
        chooser.setFileFilter(new FileNameExtensionFilter("Gaufre Save (*.gaufre)", "gaufre"));

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            try {
                GamePersistence.SaveData data = GamePersistence.load(chooser.getSelectedFile());

                this.config = data.config;
                this.model = new GaufreModel(data.rows, data.cols);
                model.restoreState(data.grid, data.currentPlayer, data.gameOver, data.loser);

                this.history = new GameHistory();
                history.restore(data.undoStack, data.redoStack);

                view.updateModel(model, config);
                view.getBoardPanel().setCellClickListener(this::handleCellClick);
                updateButtonStates();
                view.showGame();

                JOptionPane.showMessageDialog(view,
                    "Partie chargee avec succes !",
                    "Chargement", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view,
                    "Erreur lors du chargement :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateButtonStates() {
        view.getUndoBtn().setEnabled(history.canUndo() && !aiThinking);
        view.getRedoBtn().setEnabled(history.canRedo() && !aiThinking);
        view.setMoveCount(history.getMoveCount());
    }
}



// i might just go ahead and kill my self 
