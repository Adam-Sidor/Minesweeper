package com.minesweeper.backend.controller;

import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.model.Score;
import com.minesweeper.backend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public GameState startNewGame(@RequestBody StartRequest request) {
        return gameService.startNewGame(request.sessionId, request.rows, request.cols, request.mines);
    }

    @PostMapping("/testboard")
    public GameState testBoard(@RequestBody StartRequest request) {
        return gameService.generateTestBoard(request.sessionId);
    }

    @PostMapping("/firstreveal")
    public GameState firstReveal(@RequestBody RevealRequest request) {
        return gameService.firstReveal(request.sessionId, request.row, request.col);
    }

    @PostMapping("/reveal")
    public GameState reveal(@RequestBody RevealRequest request) {
        return gameService.revealCell(request.sessionId, request.row, request.col);
    }

    @PostMapping("/flag")
    public GameState click(@RequestBody FlagRequest request) {
        return gameService.flagCell(request.sessionId, request.row, request.col);
    }

    @PostMapping("/scores/get")
    public Map<String, List<Score>> getAllScores() {
        Map<String, List<Score>> allScores = new HashMap<>();
        allScores.put("easy", gameService.getScoresFromTable("easy_scores"));
        allScores.put("medium", gameService.getScoresFromTable("medium_scores"));
        allScores.put("hard", gameService.getScoresFromTable("hard_scores"));
        return allScores;
    }

    @PostMapping("/scores/save")
    public void saveScore(@RequestBody ScoreRequest request) {
        gameService.saveScore(request.name,gameService.getCurrentGame(request.sessionId).getElapsedTime(),request.difficulty);
    }

    @PostMapping("/scores/istop")
    public boolean checkTopScores(@RequestBody ScoreRequest request) {
        return gameService.checkTopScore(request.sessionId,request.difficulty);
    }

    //for some debugging - will be removed later
    @GetMapping("/showdata")
    public Map<String, String> showData(@RequestBody StartRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("clearedCells", Integer.toString(gameService.getCurrentGame(request.sessionId).getClearedCells()));
        response.put("elapsedTime", Double.toString(gameService.getCurrentGame(request.sessionId).getElapsedTime()));
        return response;
    }
}
