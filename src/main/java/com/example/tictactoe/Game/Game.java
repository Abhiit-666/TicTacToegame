package com.example.tictactoe.Game;

import com.example.tictactoe.Model.Player;
import jakarta.websocket.Session;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class Game {
    //2players.
    //currentplayer
    //game board
    //gameiD
    private String GameId;
    private Player player1;
    private Player player2;
    private char[][] board = new char[5][5];
    private Player currentPlayer;

    //constructor to initialize a game.
    public Game(Player player1, Player player2) {
        this.GameId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
    }

    //function to get gameid
    public String getGameId() {
        return GameId;
    }

    //function to startGame
    public void startGame() {
        constructUpdateBoard("");
        sendMessage(player1.getSession(), "Game Started. You are Player 1.");
        sendMessage(player1.getSession(), "Player 1 -> o Player 2 -> x");
        sendMessage(player2.getSession(), "Game Started. You are Player 2.");
        sendMessage(player2.getSession(), "Player 1 -> o Player 2 -> x");
        sendGameState(player1.getSession(),player2.getSession());
        sendMessage(player1.getSession(), "Make your move");
    }

    private void constructUpdateBoard(String move){
        String pos[]=move.split(",",2);
        //intitializing the board at start
        //need to make sure if user enters empty move after game start it wont reinitialize the board
        //check the user input inplace
        if(pos.length==0) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    board[i][j]='-';
                }
            }
        }
        else{

        }
    }

    //function to find game with a player
//        public boolean containsSession(Session session){
//            return player1.getSession().equals(session) || player2.getSession().equals(session);
//        }

    //next player to update the current player
    public Map<String,Object> nextPlayer(WebSocketSession session) {
        Map<String,Object> playerDetails=new HashMap<>();
        if (player1.getSession().equals(session)) {
            playerDetails.put("Session",player2.getSession());
            playerDetails.put("currentPlayer",player1.getPlayerId());
            return playerDetails;
        }
        playerDetails.put("Session",player1.getSession());
        playerDetails.put("ID",player2.getPlayerId());
        return playerDetails;

    }

    //function to processmove
    public void processMove(WebSocketSession session, WebSocketSession nextplayerSession,String currentPlayer, String move) {
        //find the location// user tapped on
        //a location on the board it will convert that location to the coordinate and send
        //here we check if the position is empty if so place the target there.
        //after which we check if the user has won
        //stop the game if they won
        //if not continue.
        String coords[]=move.split(",");
        checkandUpdateMove(coords,currentPlayer);
        sendGameState(session, nextplayerSession);
        sendMessage(nextplayerSession, "Make your move");
    }

    private void checkandUpdateMove(String [] move,String currentPlayer) {
        //need to determine which players move it is to determine the shape to insert
        int row = Integer.parseInt(move[0]);
        int column = Integer.parseInt(move[1]);
        if (board[row][column] == '-') {
            if(currentPlayer.equals("one")){
                board[row][column] = 'o';
                checkForWinorDraw(row,column,'o',currentPlayer);
            }
            else if(currentPlayer.equals("two")){
                board[row][column]= 'x';
                checkForWinorDraw(row,column,'x',currentPlayer);
            }
            //this would ideally be a boolean

        }
    }
     private void checkForWinorDraw(int row, int column,char symbol,String currentPlayer){

        //from currently inserted r,c I need to check for formation of//
         // 5 consequtive same symbols;
         int count=0;
         if (countSymb(row,column, 0,1,symbol) == 5||
             countSymb(row,column, 1,0,symbol) == 5||
             countSymb(row,column, 1,-1,symbol) == 5||
             countSymb(row,column, 1,1,symbol) == 5){
         endGame("Player "+currentPlayer+" has Won!!!");
         }


         for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){

            }
        }
    }

    private int countSymb(int row,int column,int rowOffset,int columnOffset,char symbol){
        //need to check if full board is filled and still no win --> for the draw scenario

        int count=0;
        //forward counting
        int r=row;
        int c=column;
        while(r>=0 && r<5 && c>=0 && c<5 && board[r][c]==symbol){
            count++;
            r+=rowOffset;
            c+=columnOffset;
        }

        r=row;
        c=column;
        while(r>=0 && r<5 && c>=0 && c<5 && board[r][c]==symbol){
            count++;
            r-=rowOffset;
            c-=columnOffset;
        }
        return count;
    }
    private String buildboard() {
        StringBuilder displayBoard = new StringBuilder();
        displayBoard.append("Board\n");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                displayBoard.append(board[i][j] + " |");
            }
            displayBoard.append("\n");
        }
        return displayBoard.toString();
    }

    //function to send game state to each player
    public void sendGameState(WebSocketSession player1session, WebSocketSession player2session) {
        String displayBoard = buildboard();

        //I need to update current player everytime there is a move
        sendMessage(player1session, displayBoard);
        sendMessage(player2session, displayBoard);
    }

    //function to notify end of game.
    public void endGame(String message) {
        sendMessage(player1.getSession(), "Game has ended " + message);
        sendMessage(player2.getSession(), "Game has ended " + message);
    }


    //helper for the sending message
    //session remote.sendText

    public void sendMessage(WebSocketSession session, String message) {
        //find someway to differentiate between a Server message and a player sending a message
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
