package com.example.tictactoe.Game;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.tictactoe.GameManager.GameManager;
import com.example.tictactoe.Model.Player;
import jakarta.websocket.Session;
import org.slf4j.ILoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.*;

public class Game {
    //2players.
    //currentplayer
    //game board
    //gameiD
    private String GameId;
    private Player player1;
    private Player player2;
    private char[][] board = new char[5][5];
    private int gameSize;
    private boolean gameEnded;
    private ScheduledExecutorService timerService;
    private Runnable timerTask;

    private final BlockingQueue<String> player1Queue= new LinkedBlockingQueue<>();
    private final BlockingQueue<String> player2Queue= new LinkedBlockingQueue<>();

    //constructor to initialize a game.
    public Game(Player player1, Player player2) {
        this.GameId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.timerService= Executors.newSingleThreadScheduledExecutor();

        startMessageProcessingThread(player1.getSession(),player1Queue);
        startMessageProcessingThread(player2.getSession(),player2Queue);
    }

    //function to get gameid
    public String getGameId() {
        return GameId;
    }



    //function to startGame
    //Start Game will take in the game Type
    //Types --> 5X5, 3X3, 4X4 blitz
    public void startGame(String type) {

        constructBoard(type);
        queueMessageForPlayer1("Game Started. You are Player 1.");
        queueMessageForPlayer1("Player 1 -> o, Player 2 -> x");
        queueMessageForPlayer2("Game Started. You are Player 2.");
        queueMessageForPlayer2("Player 1 -> o, Player 2 -> x");
        sendGameState(player1.getSession(), player2.getSession());
        queueMessageForPlayer1( "Make your move");

//        sendMessage(player1.getSession(), "Game Started. You are Player 1.");
//        sendMessage(player1.getSession(), "Player 1 -> o Player 2 -> x");
//        sendMessage(player2.getSession(), "Game Started. You are Player 2.");
//        sendMessage(player2.getSession(), "Player 1 -> o Player 2 -> x");
//        sendMessage(player1.getSession(), "Make your move");
        if(type.equalsIgnoreCase("4X4")){
            startTimer(player1.getSession());
        }
    }

    private void startTimer(WebSocketSession session){
        //Stop the executor running the Runnable
        if(timerTask!=null){
            timerService.shutdownNow();
        }
//        timerService=Executors.newSingleThreadScheduledExecutor();

        timerTask=new Runnable() {
            int timeRemaining= 10;
            @Override
            public void run() {
                if(timeRemaining > 0){

                    timeRemaining--;
                }
                else{
                    queuemessage(session,"Times up!! you missed your turn");
//                    sendMessage(session,"Times up!! you missed your turn");
                    timerService.shutdownNow();
                    switchTurn(session);
                }
            }
        };

        timerService = Executors.newSingleThreadScheduledExecutor();
        timerService.scheduleAtFixedRate(timerTask,0,1, TimeUnit.SECONDS);
    }

    public void switchTurn(WebSocketSession session){
        WebSocketSession nexPlayerSeession= (session.equals(player1.getSession())) ? player2.getSession():player1.getSession();
        System.out.println("Next Player Session: " + nexPlayerSeession);
        if(nexPlayerSeession.isOpen()) {
            queuemessage(nexPlayerSeession,"Make your move");
//            sendMessage(nexPlayerSeession, "Make your move");
            startTimer(nexPlayerSeession);
        }
        else{
            queuemessage(session,"Opponent disconnected, Closing game server!!");
        }
    }

    private void sendLiveCountdown(WebSocketSession session, int timeRemaining){
        String message="Time Remaining: "+timeRemaining+" seconds";
        //new Line and clear screen ANSI sequence for CLI display

//        sendMessage(session, message);
    }


    private void constructBoard(String Type) {
        int n=0;
        switch (Type){
            case "5X5":
                gameSize=5;
                n=5;
                break;
            case "3X3":
                gameSize=3;
                n=3;
                break;
            case "4X4":
                gameSize=4;
                n=4;
                break;
        }
        //intitializing the board at start
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    board[i][j] = '-';
                }
            }

    }

    //function to find game with a player
//        public boolean containsSession(Session session){
//            return player1.getSession().equals(session) || player2.getSession().equals(session);
//        }

    //next player to update the current player
    public Map<String, Object> nextPlayer(WebSocketSession session) {

        System.out.println(">> nextPlayer");
        Map<String, Object> playerDetails = new ConcurrentHashMap<>();
        if (player1.getSession().equals(session)) {
            playerDetails.put("Session", player2.getSession());
            playerDetails.put("currentPlayer", player1.getPlayerId());
            System.out.println("<< nextPlayer");
            return playerDetails;
        }
        playerDetails.put("Session", player1.getSession());
        playerDetails.put("currentPlayer", player2.getPlayerId());
        System.out.println("<< nextPlayer");
        return playerDetails;

    }

    //function to processmove
    public void processMove(WebSocketSession session, WebSocketSession nextplayerSession, String currentPlayer, String move) {
        //find the location// user tapped on
        //a location on the board it will convert that location to the coordinate and send
        //here we check if the position is empty if so place the target there.
        //after which we check if the user has won
        //stop the game if they won
        //if not continue.
        String coords[] = move.split(",");
        boolean update= checkandUpdateMove(coords, currentPlayer, session);
        if(update && !gameEnded){
        sendGameState(session, nextplayerSession);
        queuemessage(nextplayerSession,"Make your move");
//        sendMessage(nextplayerSession, "Make your move");
        startTimer(nextplayerSession);
        }else if (!update && !gameEnded) {
            queuemessage(session,"Make your move");
//            sendMessage(session, "Make your move");
        }

    }
    private boolean checkandUpdateMove(String[] move, String currentPlayer, WebSocketSession currentPlayerSession) {

        //need to determine which players move it is to determine the shape to insert

        //If entered coordinate are within the board.
        //If entered coordinate is not already occupied


        int row = Integer.parseInt(move[0]);
        int column = Integer.parseInt(move[1]);
        if (row >= gameSize || column >= gameSize) {
           queuemessage(currentPlayerSession,"Enter a position in the board and not occupied!!");
//            sendMessage(currentPlayerSession, "Enter a position in the board and not occupied!!");
            return false;
        }

        if (board[row][column] == '-') {
            if (currentPlayer.equals("one")) {
                board[row][column] = 'o';
                checkForWinorDraw(row, column, 'o', currentPlayer);
            } else if (currentPlayer.equals("two")) {
                board[row][column] = 'x';
                checkForWinorDraw(row, column, 'x', currentPlayer);
            }
            //this would ideally be a boolean
        } else if (board[row][column] != '-') {
            queuemessage(currentPlayerSession,"Invalid location");
//            sendMessage(currentPlayerSession, "Invalid location");
            return false;
        }
            return true;
    }

    private void checkForWinorDraw(int row, int column, char symbol, String currentPlayer) {

        //from currently inserted r,c I need to check for formation of//
        // 5 consequtive same symbols;


        //If current move leads to Win--> endGame
        //If current move leads to draw--> endGame

        int count = 0;
        if (countSymb(row, column, 0, 1, symbol) == gameSize ||
                countSymb(row, column, 1, 0, symbol) == gameSize ||
                countSymb(row, column, 1, -1, symbol) == gameSize ||
                countSymb(row, column, 1, 1, symbol) == gameSize ) {
            endGame("Player " + currentPlayer + " has Won!!!");
            gameEnded=true;
        } else if (checkDraw()) {
            endGame("The game has ended in a draw!!!");
            gameEnded=true;
        }


    }

    private boolean checkDraw() {
        for (int i = 0; i < gameSize; i++) {
            for (int j = 0; j < gameSize; j++) {
                if (board[i][j] == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    private int countSymb(int row, int column, int rowOffset, int columnOffset, char symbol) {
        //need to check if full board is filled and still no win --> for the draw scenario

        int count = 0;
        //forward counting
        int r = row;
        int c = column;
        while (r >= 0 && r < gameSize && c >= 0 && c < gameSize && board[r][c] == symbol) {
            count++;
            r += rowOffset;
            c += columnOffset;
        }

        r = row - rowOffset;
        c = column - columnOffset;
        while (r >= 0 && r < gameSize && c >= 0 && c < gameSize && board[r][c] == symbol) {
            count++;
            r -= rowOffset;
            c -= columnOffset;
        }
        return count;
    }

    private String buildboard() {
        StringBuilder displayBoard = new StringBuilder();
        displayBoard.append("Board\n");
        for (int i = 0; i < gameSize; i++) {
            for (int j = 0; j < gameSize; j++) {
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
        queuemessage(player1session,displayBoard);
        queuemessage(player2session,displayBoard);
//        queueMessageForPlayer1(displayBoard);
//        queueMessageForPlayer2(displayBoard);
    }

    //function to notify end of game.
    public void endGame(String message) {
        //Restart option on end.
        //quit option
        queueMessageForPlayer1("Game has ended: "+message);
        queueMessageForPlayer2("Game has ended: "+message);

        try{
            player1.getSession().close();
            player2.getSession().close();
        }catch(Exception e){
            e.printStackTrace();
        }

//        sendMessage(player1.getSession(), "Game has ended " + message);
//        sendMessage(player2.getSession(), "Game has ended " + message);
        gameEnded=true;
        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdownNow();  // Cleanly shut down the timer
        }
    }

    public void queueMessageForPlayer1(String message){
        player1Queue.offer(message);
    }

    public void queueMessageForPlayer2(String message){
        player2Queue.offer(message);
    }

    public void queuemessage(WebSocketSession session, String message){
        if(session.equals(player1.getSession())){
            queueMessageForPlayer1(message);
        }else{
            queueMessageForPlayer2(message);
        }
    }


    private void startMessageProcessingThread(WebSocketSession session, BlockingQueue<String> queue){
        new Thread(() -> {
            while(true){
                try{
                    String message=queue.take();
                    synchronized (session){
                        if(session.isOpen()){
                            session.sendMessage(new TextMessage(message));
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //helper for the sending message
    //session remote.sendText

//    public void sendMessage(WebSocketSession session, String message) {
//        //find someway to differentiate between a Server message and a player sending a message
//        try {
//            synchronized (session) {
//                session.sendMessage(new TextMessage(message));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
