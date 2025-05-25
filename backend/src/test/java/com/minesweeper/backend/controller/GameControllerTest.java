package com.minesweeper.backend.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minesweeper.backend.model.GameState;
import com.minesweeper.backend.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    static class StartRequest {
        public int rows;
        public int cols;
        public int mines;
        public String sessionId;
        public StartRequest(String sessionId,int r, int c, int m) {
            this.rows = r;
            this.cols = c;
            this.mines = m;
            this.sessionId = sessionId;
        }
    }

    @Test
    public void testStartNewGame() throws Exception {
        String sessionId = "test";
        GameState dummyGame = new GameState(null, GameState.GameStatus.IN_PROGRESS, 10);
        when(gameService.startNewGame(sessionId,9, 9, 10)).thenReturn(dummyGame);

        StartRequest request = new StartRequest(sessionId,9, 9, 10);

        mockMvc.perform(post("/api/game/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void flagCell_returnsUpdatedGameState() throws Exception {
        String sessionId = "test";
        FlagRequest request = new FlagRequest(sessionId,1, 2);

        GameState dummyGame = new GameState(null, GameState.GameStatus.IN_PROGRESS, 10);
        when(gameService.flagCell(sessionId,1, 2)).thenReturn(dummyGame);

        mockMvc.perform(post("/api/game/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

}
