package com.minesweeper.backend.controller;

public class FlagRequest {
    public int row;
    public int col;

    public FlagRequest(int row, int col) {
        this.row = row;
        this.col = col;
    }
}

