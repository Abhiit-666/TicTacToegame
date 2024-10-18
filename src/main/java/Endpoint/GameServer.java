package Endpoint;

import com.example.tictactoe.GameManager.GameManager;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/game")
public class GameServer
{
    private static GameManager gameManager;


    @OnOpen
    public void onOpen(Session session){
        System.out.println("New player connected: "+session.getId());
        gameManager.addPlayer(session);
    }

    @OnMessage
    public void onMessage(String message,Session session){
        gameManager.processMessage(session,message);
    }

    @OnClose
    public void onClose(Session session,String reason){
        System.out.println("Connection closed: "+session.getId());
        gameManager.disconnectPlayer(session,reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable){
        System.err.println("Error in session"+session.getId()+ ": "+throwable.getMessage());
    }
}
