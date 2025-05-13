package com.minesweeper.backend.model;

import java.util.List;

public class GameState {
    public enum CellState {
        HIDDEN, REVEALED, FLAGGED
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

    public GameState(List<List<Cell>> board) {
        this.board = board;
    }

    public List<List<Cell>> getBoard() {
        return board;
    }
}
