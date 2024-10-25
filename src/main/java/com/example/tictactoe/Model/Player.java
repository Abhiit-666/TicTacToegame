package com.example.tictactoe.Model;

import jakarta.websocket.Session;
import org.springframework.web.socket.WebSocketSession;

public class Player {

    private WebSocketSession session;
    private String playerId;

    public Player(WebSocketSession session){
        this.session=session;
    }

    public WebSocketSession getSession(){
        return session;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
