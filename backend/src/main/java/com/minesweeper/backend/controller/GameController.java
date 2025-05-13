package com.minesweeper.backend.controller;

import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService = new GameService();

    @GetMapping("/start")
    public GameState startNewGame() {
        return gameService.startNewGame(8, 8, 10);
    }

    @PostMapping("/click")
    public GameState click(@RequestBody ClickRequest request) {
        return gameService.revealCell(request.row, request.col);
    }

}
