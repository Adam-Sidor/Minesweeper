package com.minesweeper.backend.model;

import java.time.LocalDateTime;

public class Score {
    private String name;
    private int time;
    private LocalDateTime date;

    public Score(String name, int time, LocalDateTime date) {
        this.name = name;
        this.time = time;
        this.date = date;
    }

    public Score(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
