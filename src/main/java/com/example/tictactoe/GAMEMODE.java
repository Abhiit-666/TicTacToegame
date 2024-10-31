package com.example.tictactoe;

public enum GAMEMODE {
    MODE_1("5X5"),
    MODE_2("3X3"),
    MODE_3("4X4");

    private final String boardSize;
    GAMEMODE(String boardSize){
        this.boardSize=boardSize;
    }

    public String getBoardSize(){
        return boardSize;
    }
}
