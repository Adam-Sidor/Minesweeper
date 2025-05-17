package com.minesweeper.backend.service;

import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
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

        currentGame = new GameState(board, GameState.GameStatus.IN_PROGRESS,mines);
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
                currentGame.incrementRemainingMines();
            }
            case HIDDEN -> {
                cell.setState(GameState.CellState.FLAGGED);
                currentGame.decrementRemainingMines();
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
            gameOver(rows, cols);
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
                        gameOver(rows, cols);
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

    private void gameOver(int rows, int cols) {
        currentGame.setStatus(GameState.GameStatus.LOST);

        currentGame.stopTimer();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if(currentGame.getCell(r,c).hasMine && currentGame.getCell(r,c).state != GameState.CellState.FLAGGED){
                    currentGame.getCell(r,c).setState(GameState.CellState.REVEALED);
                }
            }
        }
    }

    public GameState getCurrentGame() {
        return currentGame;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveScore(String name, double time, String difficulty) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);
        try{
            switch (difficulty) {
                case "easy" -> jdbcTemplate.update("INSERT INTO easy_scores (name, time, date) VALUES (?, ?, ?)",
                        name, time, formattedDate);
                case "medium" -> jdbcTemplate.update("INSERT INTO medium_scores (name, time, date) VALUES (?, ?, ?)",
                        name, time, formattedDate);
                case "hard" -> jdbcTemplate.update("INSERT INTO hard_scores (name, time, date) VALUES (?, ?, ?)",
                        name, time, formattedDate);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean checkTopScore(String difficulty) {
        if(!difficulty.equals("custom")){
            String tableName = switch (difficulty) {
                case "easy" -> "easy_scores";
                case "medium" -> "medium_scores";
                case "hard" -> "hard_scores";
                default -> throw new IllegalArgumentException("Nieznana trudność: " + difficulty);
            };
            String sql = "SELECT time FROM " + tableName + " ORDER BY time ASC LIMIT 10";

            List<Double> topTimes = jdbcTemplate.queryForList(sql, Double.class);

            if(topTimes.size() < 10){
                return true;
            }else{
                return topTimes.get(9) > currentGame.getElapsedTime();
            }
        }
        return false;
    }

    public List<Score> getScoresFromTable(String tableName) {
        String sql = "SELECT name, time, date FROM " + tableName + " ORDER BY time ASC LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Score(
                        rs.getString("name"),
                        rs.getInt("time"),
                        rs.getTimestamp("date").toLocalDateTime()
                )
        );
    }

}
