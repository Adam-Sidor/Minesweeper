package com.minesweeper.backend.service;

import com.minesweeper.backend.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
    }

    @Test
    void testStartNewGame() {
        int rows = 5;
        int cols = 6;
        int mines = 10;
        String sessionId = "test";
        GameState game = gameService.startNewGame(sessionId,rows, cols, mines);

        assertNotNull(game);
        assertEquals(GameState.GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(rows, game.getBoard().size());
        assertEquals(cols, game.getBoard().get(0).size());
    }

    @Test
    void testRevealEmptyCell_shouldRevealAndStopOnNumber() {
        String sessionId = "test";
        gameService.generateTestBoard(sessionId);

        GameState result = gameService.revealCell(sessionId,0, 0);

        assertEquals(GameState.CellState.REVEALED, result.getCell(0, 0).getState(), "Cell [0][0] should be revealed");
        assertEquals(GameState.CellState.REVEALED, result.getCell(0, 1).getState(), "Cell [0][1] should also be revealed");

        for (int i = 2; i < 9; i++) {
            assertEquals(GameState.CellState.HIDDEN, result.getCell(0, i).getState(), "Cell [0][" + i + "] should remain hidden");
        }

        assertEquals(GameState.CellState.HIDDEN, result.getCell(0, 9).getState(), "Mine should remain hidden");
    }

    @Test
    void testFlaggingCells() {
        String sessionId = "test";
        gameService.startNewGame(sessionId,2, 2, 1);

        GameState.Cell cell = gameService.getCurrentGame(sessionId).getCell(0, 0);
        assertEquals(GameState.CellState.HIDDEN, cell.getState());

        gameService.flagCell(sessionId,0, 0);
        assertEquals(GameState.CellState.FLAGGED, cell.getState());
        assertEquals(0, gameService.getCurrentGame(sessionId).getRemainingMines(),"Remaining mines should be decreased");

        gameService.flagCell(sessionId,0, 0);
        assertEquals(GameState.CellState.HIDDEN, cell.getState());
        assertEquals(1, gameService.getCurrentGame(sessionId).getRemainingMines(),"Remaining mines should be increased");

        gameService.revealCell(sessionId,0,0);
        gameService.flagCell(sessionId,0, 0);
        assertEquals(GameState.CellState.REVEALED, cell.getState());
    }

    @Test
    void testGameOverWhenMineRevealed() {
        String sessionId = "test";
        gameService.startNewGame(sessionId,2, 2, 1);

        gameService.getCurrentGame(sessionId).getCell(0, 0).hasMine = true;

        GameState result = gameService.revealCell(sessionId,0, 0);

        assertEquals(GameState.GameStatus.LOST, result.getStatus(), "Game should be lost");
        assertEquals(GameState.CellState.REVEALED, result.getCell(0, 0).getState(), "Mine cell should be revealed");

        for (List<GameState.Cell> row : result.getBoard()) {
            for (GameState.Cell cell : row) {
                if (cell.hasMine) {
                    assertEquals(GameState.CellState.REVEALED, cell.getState(),"All mines should be revealed");
                }
            }
        }
    }

    @Test
    void testWinWhenAllNonMineCellsRevealed() {
        String sessionId = "test";
        gameService.startNewGame(sessionId,2, 2, 1);

        gameService.getCurrentGame(sessionId).getCell(0, 0).hasMine = true;
        gameService.getCurrentGame(sessionId).getCell(0, 1).adjacentMines = 1;
        gameService.getCurrentGame(sessionId).getCell(1, 0).adjacentMines = 1;
        gameService.getCurrentGame(sessionId).getCell(1, 1).adjacentMines = 1;

        gameService.revealCell(sessionId,0, 1);
        gameService.revealCell(sessionId,1, 0);
        GameState result = gameService.revealCell(sessionId,1, 1);

        assertEquals(GameState.GameStatus.WON, result.getStatus(), "Game should be won");

        for (List<GameState.Cell> row : result.getBoard()) {
            for (GameState.Cell cell : row) {
                if (cell.state == GameState.CellState.REVEALED) {
                    assertFalse(cell.hasMine,"Revealed cell should not have mine");
                }
            }
        }
    }


}
