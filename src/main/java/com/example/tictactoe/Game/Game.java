package com.example.tictactoe.Game;

import com.example.tictactoe.Model.Player;
import jakarta.websocket.Session;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

public class Game {
        //2players.
        //currentplayer
        //game board
        //gameiD
        private String GameId;
        private Player player1;
        private Player player2;
        private char[][] board= new char[3][3];
        private Player currentPlayer;

        //constructor to initialize a game.
        public Game(Player player1, Player player2){
            this.GameId= UUID.randomUUID().toString();
            this.player1=player1;
            this.player2=player2;
            this.currentPlayer=player1;
        }

        //function to get gameid
        public String getGameId(){
            return GameId;
        }
        //function to startGame
        public void startGame(){
            sendMessage(player1.getSession(),"Game Started. You are Player 1.");
            sendMessage(player2.getSession(),"Game Started. You are Player 2.");
            sendGameState();
        }
        //function to find game with a player
//        public boolean containsSession(Session session){
//            return player1.getSession().equals(session) || player2.getSession().equals(session);
//        }

            //next player to update the current player
            public WebSocketSession nextPlayer(WebSocketSession session){
                if(player1.getSession().equals(session)){
                    return player2.getSession();
                }
                return player1.getSession();
            }

        //function to processmove
        public void processMove(WebSocketSession session,WebSocketSession nextplayerSession, String move){
            //find the location// user tapped on
            //a location on the board it will convert that location to the coordinate and send
            //here we check if the position is empty if so place the target there.
            //after which we check if the user has won
            //stop the game if they won
            //if not continue
            sendGameState();
            sendMessage(nextplayerSession,"Make your move");
        }

        //function to send game state to each player
        public void sendGameState(){

            //i need to update current player everytime their is a move
        }
        //function to notify end of game.
        public void endGame(String message){
            sendMessage(player1.getSession(),"Game has ended "+message);
            sendMessage(player2.getSession(),"Game has ended "+message);
        }


        //helper for the sending message
        //session remote.sendText

        public void sendMessage(WebSocketSession session,String message){
            //find someway to differentiate between a Server message and a player sending a message
        try{
            session.sendMessage(new TextMessage(message));
        }catch(Exception e)
        {
                e.printStackTrace();
        }
        }
}
