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
    }

    private List<List<Cell>> board;
    private GameStatus status;

    public GameState(List<List<Cell>> board, GameStatus status) {
        this.board = board;
        this.status = status;
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
}
