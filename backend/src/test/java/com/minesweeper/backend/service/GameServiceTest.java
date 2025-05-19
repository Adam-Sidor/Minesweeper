package com.minesweeper.backend.service;

import com.minesweeper.backend.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        GameState game = gameService.startNewGame(rows, cols, mines);

        assertNotNull(game);
        assertEquals(GameState.GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(rows, game.getBoard().size());
        assertEquals(cols, game.getBoard().get(0).size());
    }

    @Test
    void testRevealEmptyCell_shouldRevealAndStopOnNumber() {
        gameService.generateTestBoard();

        GameState result = gameService.revealCell(0, 0);

        assertEquals(GameState.CellState.REVEALED, result.getCell(0, 0).getState(), "Cell [0][0] should be revealed");
        assertEquals(GameState.CellState.REVEALED, result.getCell(0, 1).getState(), "Cell [0][1] should also be revealed");

        for (int i = 2; i < 9; i++) {
            assertEquals(GameState.CellState.HIDDEN, result.getCell(0, i).getState(), "Cell [0][" + i + "] should remain hidden");
        }

        assertEquals(GameState.CellState.HIDDEN, result.getCell(0, 9).getState(), "Mine should remain hidden");
    }

    @Test
    void testFlaggingCells() {
        gameService.startNewGame(2, 2, 1);

        GameState.Cell cell = gameService.getCurrentGame().getCell(0, 0);
        assertEquals(GameState.CellState.HIDDEN, cell.getState());

        gameService.flagCell(0, 0);
        assertEquals(GameState.CellState.FLAGGED, cell.getState());
        assertEquals(0, gameService.getCurrentGame().getRemainingMines(),"Remaining mines should be decreased");

        gameService.flagCell(0, 0);
        assertEquals(GameState.CellState.HIDDEN, cell.getState());
        assertEquals(1, gameService.getCurrentGame().getRemainingMines(),"Remaining mines should be increased");

        gameService.revealCell(0,0);
        gameService.flagCell(0, 0);
        assertEquals(GameState.CellState.REVEALED, cell.getState());
    }

}
