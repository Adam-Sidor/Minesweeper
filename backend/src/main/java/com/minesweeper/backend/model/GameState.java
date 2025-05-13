package com.minesweeper.backend.model;

import java.util.List;

public class GameState {
    public enum CellState {
        HIDDEN, REVEALED, FLAGGED
    }

    public enum GameStatus {
        IN_PROGRESS, LOST, WON
    }

    public static class Cell {
        public boolean hasMine;
        public int adjacentMines;
        public CellState state;

        public Cell(boolean hasMine, int adjacentMines) {
            this.hasMine = hasMine;
            this.adjacentMines = adjacentMines;
            this.state = CellState.HIDDEN;
        }

        public CellState getState() {
            return state;
        }

        public void setState(CellState state) {
            this.state = state;
        }
    }

    private List<List<Cell>> board;
    private GameStatus status;
    private int clearedCells;
    private int remainingMines;

    private long startTime;
    private long endTime;

    public GameState(List<List<Cell>> board, GameStatus status, int remainingMines) {
        this.board = board;
        this.status = status;
        clearedCells = 0;
        this.remainingMines = remainingMines;
        startTime = System.currentTimeMillis();
    }

    public double getElapsedTime() {
        return endTime > 0 ? (endTime - startTime) / 1000.0 : (System.currentTimeMillis() - startTime) / 1000.0;
    }

    public void stopTimer() {
        endTime = System.currentTimeMillis();
    }

    public List<List<Cell>> getBoard() {
        return board;
    }

    public Cell getCell(int row, int column) {
        return board.get(row).get(column);
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getClearedCells() {
        return clearedCells;
    }

    public void incrementClearedCells() {
        this.clearedCells += 1;
    }
    public int getRemainingMines() {
        return remainingMines;
    }
    public void incrementRemainingMines() {
        this.remainingMines += 1;
    }
    public void decrementRemainingMines() {
        this.remainingMines -= 1;
    }
}
