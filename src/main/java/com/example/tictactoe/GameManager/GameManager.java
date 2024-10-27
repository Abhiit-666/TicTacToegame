package com.example.tictactoe.GameManager;

import com.example.tictactoe.GAMEMODE;
import com.example.tictactoe.Game.Game;
import com.example.tictactoe.Model.Player;
import jakarta.websocket.Session;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameManager {


    private Map<GAMEMODE,Map<String, Player>> waitingPlayers = new ConcurrentHashMap<>();
    private Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private Map<WebSocketSession, Game> playertogameMap = new ConcurrentHashMap<>();
    private Map<Player,String> gameLobby=new ConcurrentHashMap<>();
    //add Player.
    public void addPlayer(WebSocketSession session) {
        Player player = new Player(session);
        gameLobby.put(player,)
        if (waitingPlayers.isEmpty()) {
            waitingPlayers.put(session.getId(), player);
        } else {
            Player opponent = waitingPlayers.values().iterator().next();
            player.setPlayerId("one");
            opponent.setPlayerId("two");
            Game game = new Game(player, opponent);
            playertogameMap.put(player.getSession(), game);
            playertogameMap.put(opponent.getSession(), game);
            activeGames.put(game.getGameId(), game);
            waitingPlayers.remove(opponent.getSession().getId());
            game.startGame();
        }
    }

    private void createLobby(WebSocketSession session, String message){
        GAMEMODE mode;
        switch (message){
            case "MODE_1":
                waitingPlayers.put(GAMEMODE.MODE_1,new ConcurrentHashMap<>());
                mode= GAMEMODE.MODE_1;
                break;
            case "MODE_2":
                waitingPlayers.put(GAMEMODE.MODE_2,new ConcurrentHashMap<>());
                mode= GAMEMODE.MODE_2;
                break;
            case "MODE_3":
                waitingPlayers.put(GAMEMODE.MODE_3,new ConcurrentHashMap<>());
                mode= GAMEMODE.MODE_3;
                break;
        }

    }
    //Process player Move
    //we have a player session representing the current player.
    //we also have that player move which is represented by a string
    //We have to p first find an active game by session(player)
    //once that is found we have to process the players move in that active game
    public void processMessage(WebSocketSession session, String message) {
        System.out.println(">> procecssMessage");

        if(message.contains("MODE")){
            createLobby(session,message);
        }
        else{
        Game game = playertogameMap.get(session);
        if (game != null) {
            Map<String, Object> playerDetails = game.nextPlayer(session);
            System.out.println("PPlayer Details: "+ playerDetails);
            WebSocketSession oppositionSession = (WebSocketSession) playerDetails.get("Session");
            String currentPlayer1 = (String) playerDetails.get("currentPlayer");
            System.out.println("pcurrentPlayer: " + currentPlayer1);
            if (message.contains("/text")) {
                String messagecontent[] = message.split(" ");
                StringBuilder messagebuilder = new StringBuilder();
                for (int i = 1; i < messagecontent.length; i++) {
                    messagebuilder.append(messagecontent[i] + " ");
                }
                String finalMessage = "Opponent" + " :" + messagebuilder.toString().trim();
//                System.out.println(messagebuilder.toString().trim());
                game.sendMessage(oppositionSession, finalMessage);
                System.out.println("<< procecssMessage");
            } else {
                game.processMove(session, oppositionSession, currentPlayer1, message);
                System.out.println("<< procecssMessage");
            }

        }
    }
    }


    //disconnetplayer or end game
    //we firt find an active game for the session or the player.
    //if a game is present end the game and remove it from the games list.
    //if no games are present for the session meaning the player must be in the waiting list.
    //remove the player from the waiting list.
    public void disconnectPlayer(WebSocketSession session) {
        Game game = playertogameMap.get(session);
        if (game != null) {
            game.endGame("Game Ended");
            activeGames.remove(game.getGameId());
        }
    }

    //find games by players(Sessions)
    //session is provided
    //we need to iterate the active games list and find
    //if any games has the session or player.
//    private Game findGameBySession(Session session){
//        for(Game game: activeGames.values()){
//            if(game.containsSession(session)){
//                return game;
//            }
//        }
//        return null;
//    }
//


}
