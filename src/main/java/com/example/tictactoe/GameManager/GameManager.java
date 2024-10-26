package com.example.tictactoe.GameManager;

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


    private Map<String, Player> waitingPlayers = new ConcurrentHashMap<>();
    private Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private Map<WebSocketSession, Game> playertogameMap = new ConcurrentHashMap<>();

    //add Player.
    public void addPlayer(WebSocketSession session) {
        Player player = new Player(session);
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

    //Process player Move
    //we have a player session representing the current player.
    //we also have that player move which is represented by a string
    //We have to p first find an active game by session(player)
    //once that is found we have to process the players move in that active game
    public void processMessage(WebSocketSession session, String message) {
        System.out.println(">> procecssMessage");
        Game game = playertogameMap.get(session);
        if (game != null) {
            Map<String, Object> playerDetails=game.nextPlayer(session);
            WebSocketSession oppositionSession= (WebSocketSession) playerDetails.get("Session");
            String currentPlayer=(String) playerDetails.get("currentPlayer");
            System.out.println("pplayer session: "+ oppositionSession);
            System.out.println("pcurrentPlayer: "+ currentPlayer);
            if (message.contains("/text")){
                String messagecontent[]=message.split(" ");
                StringBuilder messagebuilder= new StringBuilder();
                for(int i=1;i<messagecontent.length;i++){
                    messagebuilder.append(messagecontent[i]+" ");
                }
                String finalMessage = "Opponent" + " :" + messagebuilder.toString().trim();
//                System.out.println(messagebuilder.toString().trim());
                game.sendMessage(oppositionSession,finalMessage);
                System.out.println("<< procecssMessage");
            }else{
                game.processMove(session,oppositionSession,currentPlayer, message);
                System.out.println("<< procecssMessage");
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
