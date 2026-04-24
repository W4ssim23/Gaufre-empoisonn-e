package model;

import java.io.Serializable;

/**
 * Configuration d'une partie de Gaufre.
 */
public class GameConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rows;
    private int cols;
    private boolean vsAI;
    private boolean aiStarts;
    private int aiPlayer; // 1 ou 2

    public GameConfig() {
        this.rows = 5;
        this.cols = 7;
        this.vsAI = false;
        this.aiStarts = false;
        this.aiPlayer = 2;
    }

    public GameConfig(int rows, int cols, boolean vsAI, boolean aiStarts) {
        this.rows = rows;
        this.cols = cols;
        this.vsAI = vsAI;
        this.aiStarts = aiStarts;
        this.aiPlayer = aiStarts ? 1 : 2;
    }

    // Getters & Setters

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public boolean isVsAI() {
        return vsAI;
    }

    public void setVsAI(boolean vsAI) {
        this.vsAI = vsAI;
    }

    public boolean isAiStarts() {
        return aiStarts;
    }

    public void setAiStarts(boolean aiStarts) {
        this.aiStarts = aiStarts;
        this.aiPlayer = aiStarts ? 1 : 2;
    }

    public int getAiPlayer() {
        return aiPlayer;
    }

    public void setAiPlayer(int aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    @Override
    public String toString() {
        return "Config[" + rows + "x" + cols +
                (vsAI ? ", vs IA" + (aiStarts ? " (IA commence)" : " (Humain commence)") : ", Humain vs Humain") + "]";
    }
}
