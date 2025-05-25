package com.minesweeper.backend.controller;

import java.util.UUID;

public class FlagRequest {
    public String sessionId;
    public int row;
    public int col;

    public FlagRequest(String sessionId,int row, int col) {
        this.sessionId = sessionId;
        this.row = row;
        this.col = col;
    }
}

