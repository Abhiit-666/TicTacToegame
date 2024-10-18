package com.example.tictactoe;


import org.springframework.web.server.WebSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private GameManager gameManager= new GameManager();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        gameManager.addPlayer(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        gameManager.processMessage(session,message.getPayload());
    }

    @Override
    public void afterConnnectionclosed(WebSocketSession session) throws IOExpection
    {
        gameManager.removePlayer(session);
    }


    
}
