package com.minesweeper.backend.service;

import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static java.lang.Math.abs;

@Service
public class GameService {

    private final Map<String, GameState> games = new HashMap<>();
    private final Map<String, Integer> gameMines = new HashMap<>();

    public GameState startNewGame(String sessionId, int rows, int cols, int mines) {
        List<List<GameState.Cell>> board = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            List<GameState.Cell> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(new GameState.Cell(false, 0));
            }
            board.add(row);
        }

        GameState game = new GameState(board, GameState.GameStatus.IN_PROGRESS,mines);
        games.put(sessionId, game);
        gameMines.put(sessionId, mines);
        return game;
    }

    public GameState getGame(String sessionId) {
        return games.get(sessionId);
    }

    public int getMines(String sessionId) {
        return gameMines.get(sessionId);
    }

    public GameState firstReveal(String sessionId, int firstRow, int firstCol) {
        int rows = getGame(sessionId).getBoard().size();
        int cols = getGame(sessionId).getBoard().get(0).size();
        int mines = getMines(sessionId);

        Random random = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!getGame(sessionId).getCell(r,c).hasMine && (abs(firstRow-r)>1 || abs(firstCol-c)>1)) {
                getGame(sessionId).getCell(r,c).hasMine = true;
                placed++;
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!getGame(sessionId).getCell(r,c).hasMine) {
                    int count = 0;
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int nr = r + i;
                            int nc = c + j;
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && getGame(sessionId).getCell(nr,nc).hasMine) {
                                count++;
                            }
                        }
                    }
                    getGame(sessionId).getCell(r,c).adjacentMines = count;
                }
            }
        }
        revealCell(sessionId,firstRow,firstCol);
        getGame(sessionId).startTimer();
        getGame(sessionId).setStatus(GameState.GameStatus.IN_PROGRESS);
        return getGame(sessionId);
    }


    public GameState generateTestBoard(String sessionId) {
        List<List<GameState.Cell>> board = new ArrayList<>();

        List<GameState.Cell> row = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            row.add(new GameState.Cell(false, 0));
        }
        board.add(row);

        board.get(0).get(9).hasMine = true;

        for (int i = 0; i < 9; i++) {
            board.get(0).get(i).adjacentMines = i;
        }

        GameState game = new GameState(board, GameState.GameStatus.IN_PROGRESS,1);
        games.put(sessionId, game);
        gameMines.put(sessionId, 1);
        return game;

    }

    public GameState flagCell(String sessionId,int row, int col){
        if (getGame(sessionId) == null) return null;

        GameState.Cell cell = getGame(sessionId).getCell(row,col);
        switch (cell.state){
            case REVEALED -> {
                return getGame(sessionId);
            }
            case FLAGGED -> {
                cell.setState(GameState.CellState.HIDDEN);
                getGame(sessionId).incrementRemainingMines();
            }
            case HIDDEN -> {
                cell.setState(GameState.CellState.FLAGGED);
                getGame(sessionId).decrementRemainingMines();
            }
        }
        return getGame(sessionId);
    }

    public GameState revealCell(String sessionId,int row, int col) {
        if (getGame(sessionId) == null) return null;

        int rows = getGame(sessionId).getBoard().size();
        int cols = getGame(sessionId).getBoard().get(0).size();

        GameState.Cell cell = getGame(sessionId).getCell(row,col);

        if (cell.hasMine) {
            gameOver(sessionId,rows, cols);
            return getGame(sessionId);
        }

        if(cell.state == GameState.CellState.REVEALED){
            revealNeighbors(sessionId,row, col, false);
            return getGame(sessionId);
        }

        if (cell.state == GameState.CellState.FLAGGED) {
            return getGame(sessionId);
        }

        cell.state = GameState.CellState.REVEALED;
        getGame(sessionId).incrementClearedCells();

        if (cell.adjacentMines == 0) {
            revealNeighbors(sessionId, row, col, true);
        }

        checkWin(sessionId,getGame(sessionId), rows, cols);

        return getGame(sessionId);
    }

    private void checkWin(String sessionId, GameState currentGame, int rows, int cols) {
        if(currentGame.getClearedCells() == rows * cols - getMines(sessionId)){
            currentGame.setStatus(GameState.GameStatus.WON);
            currentGame.stopTimer();
        }
    }

    private void revealNeighbors(String sessionId,int row, int col, boolean ignoreMines) {
        int rows = getGame(sessionId).getBoard().size();
        int cols = getGame(sessionId).getBoard().get(0).size();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = row + i;
                int nc = col + j;

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    GameState.Cell neighbor = getGame(sessionId).getCell(nr,nc);
                    if(!ignoreMines && neighbor.hasMine && neighbor.state != GameState.CellState.FLAGGED) {
                        gameOver(sessionId,rows, cols);
                    }
                    if (neighbor.state == GameState.CellState.HIDDEN && !neighbor.hasMine) {
                        neighbor.state = GameState.CellState.REVEALED;
                        getGame(sessionId).incrementClearedCells();
                        if (neighbor.adjacentMines == 0) {
                            revealNeighbors(sessionId,nr, nc, ignoreMines);
                        }
                    }
                }
            }
        }
        checkWin(sessionId,getGame(sessionId), rows, cols);
    }

    private void gameOver(String sessionId,int rows, int cols) {
        getGame(sessionId).setStatus(GameState.GameStatus.LOST);

        getGame(sessionId).stopTimer();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if(getGame(sessionId).getCell(r,c).hasMine && getGame(sessionId).getCell(r,c).state != GameState.CellState.FLAGGED){
                    getGame(sessionId).getCell(r,c).setState(GameState.CellState.REVEALED);
                }
            }
        }
    }

    public GameState getCurrentGame(String sessionId) {
        return getGame(sessionId);
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

    public boolean checkTopScore(String sessionId,String difficulty) {
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
                return topTimes.get(9) > getGame(sessionId).getElapsedTime();
            }
        }
        return false;
    }

    public List<Score> getScoresFromTable(String tableName) {
        String sql = "SELECT name, time, date FROM " + tableName + " ORDER BY time ASC LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Score(
                        rs.getString("name"),
                        rs.getDouble("time"),
                        rs.getTimestamp("date").toLocalDateTime()
                )
        );
    }

}
