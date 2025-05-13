package com.minesweeper.backend.service;

import com.minesweeper.backend.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService {

    private GameState currentGame;
    private int mines;

    public GameState startNewGame(int rows, int cols, int mines) {
        List<List<GameState.Cell>> board = new ArrayList<>();
        this.mines = mines;

        for (int i = 0; i < rows; i++) {
            List<GameState.Cell> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(new GameState.Cell(false, 0));
            }
            board.add(row);
        }

        Random random = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!board.get(r).get(c).hasMine) {
                board.get(r).get(c).hasMine = true;
                placed++;
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board.get(r).get(c).hasMine) {
                    int count = 0;
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int nr = r + i;
                            int nc = c + j;
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && board.get(nr).get(nc).hasMine) {
                                count++;
                            }
                        }
                    }
                    board.get(r).get(c).adjacentMines = count;
                }
            }
        }

        currentGame = new GameState(board, GameState.GameStatus.IN_PROGRESS);
        return currentGame;

    }

    public GameState flagCell(int row, int col){
        if (currentGame == null) return null;

        GameState.Cell cell = currentGame.getCell(row,col);
        switch (cell.state){
            case REVEALED -> {
                return currentGame;
            }
            case FLAGGED -> {
                cell.setState(GameState.CellState.HIDDEN);
            }
            case HIDDEN -> {
                cell.setState(GameState.CellState.FLAGGED);
            }
        }
        return currentGame;
    }

    public GameState revealCell(int row, int col) {
        if (currentGame == null) return null;

        int rows = currentGame.getBoard().size();
        int cols = currentGame.getBoard().get(0).size();

        GameState.Cell cell = currentGame.getCell(row,col);

        if (cell.hasMine) {
            currentGame.setStatus(GameState.GameStatus.LOST);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if(currentGame.getCell(r,c).hasMine){
                        currentGame.getCell(r,c).setState(GameState.CellState.REVEALED);
                    }
                }
            }
            return currentGame;
        }

        if(cell.state == GameState.CellState.REVEALED){
            revealNeighbors(row, col, false);
            return currentGame;
        }

        if (cell.state == GameState.CellState.FLAGGED) {
            return currentGame;
        }

        cell.state = GameState.CellState.REVEALED;
        currentGame.incrementClearedCells();

        if (cell.adjacentMines == 0) {
            revealNeighbors(row, col, true);
        }

        if(currentGame.getClearedCells() == rows * cols - mines){
            currentGame.setStatus(GameState.GameStatus.WON);
        }

        return currentGame;
    }

    private void revealNeighbors(int row, int col, boolean ignoreMines) {
        int rows = currentGame.getBoard().size();
        int cols = currentGame.getBoard().get(0).size();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = row + i;
                int nc = col + j;

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    GameState.Cell neighbor = currentGame.getCell(nr,nc);
                    if(!ignoreMines && neighbor.hasMine && neighbor.state != GameState.CellState.FLAGGED) {
                        currentGame.setStatus(GameState.GameStatus.LOST);
                    }
                    if (neighbor.state == GameState.CellState.HIDDEN && !neighbor.hasMine) {
                        neighbor.state = GameState.CellState.REVEALED;
                        currentGame.incrementClearedCells();
                        if (neighbor.adjacentMines == 0) {
                            revealNeighbors(nr, nc, ignoreMines);
                        }
                    }
                }
            }
        }
    }

    public GameState getCurrentGame() {
        return currentGame;
    }
}
