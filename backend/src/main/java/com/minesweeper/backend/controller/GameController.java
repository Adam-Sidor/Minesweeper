package com.minesweeper.backend.controller;

import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService = new GameService();

    @PostMapping("/start")
    public GameState startNewGame(@RequestBody StartRequest request) {
        return gameService.startNewGame(request.rows, request.cols, request.mines);
    }

    @PostMapping("/reveal")
    public GameState click(@RequestBody RevealRequest request) {
        return gameService.revealCell(request.row, request.col);
    }

    @PostMapping("/flag")
    public GameState click(@RequestBody FlagRequest request) {
        return gameService.flagCell(request.row, request.col);
    }

    //for some debugging - will be removed later
    @GetMapping("/showdata")
    public Map<String, String> showData() {
        Map<String, String> response = new HashMap<>();
        response.put("clearedCells", Integer.toString(gameService.getCurrentGame().getClearedCells()));
        response.put("elapsedTime", Double.toString(gameService.getCurrentGame().getElapsedTime()));
        return response;
    }
}
