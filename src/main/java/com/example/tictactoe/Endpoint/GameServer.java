package com.example.tictactoe.Endpoint;

import com.example.tictactoe.GameManager.GameManager;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.web.socket.WebSocketSession;

@ServerEndpoint("/game")
public class GameServer
{
    private static GameManager gameManager;


    @OnOpen 
    public void onOpen(WebSocketSession session){
        System.out.println("New player connected: "+session.getId());
        gameManager.addPlayer(session);
    }

    @OnMessage
    public void onMessage(String message,WebSocketSession session){
        gameManager.processMessage(session,message);
    }

    @OnClose
    public void onClose(WebSocketSession session){
        System.out.println("Connection closed: "+session.getId());
        gameManager.disconnectPlayer(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable){
        System.err.println("Error in session"+session.getId()+ ": "+throwable.getMessage());
    }
}
