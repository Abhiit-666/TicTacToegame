package com.example.tictactoe.client;

import jakarta.websocket.*;

import java.net.URI;
import java.util.Scanner;

@ClientEndpoint
public class TicTacToeClient {
    private Session session;

    public void connectToServer(String serverURI) throws Exception{
        WebSocketContainer container= ContainerProvider.getWebSocketContainer();
        container.connectToServer(this,new URI(serverURI));
    }

    @OnMessage
    public void onMessage(String message){
        System.out.println("Server :" + message);
    }


    @OnOpen
    public void onOpen(Session session){
        this.session= session;
        System.out.println("Connected to the server");
    }

    @OnClose
    public void onClose(Session session,CloseReason closeReason){
        System.out.println("Connection closed" +closeReason);
    }

    public void sendMessage(String message){
        try{
            session.getBasicRemote().sendText(message);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public static void main(String args[]) throws Exception{
        TicTacToeClient client= new TicTacToeClient();
        client.connectToServer("ws://localhost:8080/game");

        Scanner sc=new Scanner(System.in);

        while(true){
            System.out.println("Connected Top Server");
            String move=sc.nextLine();
            client.sendMessage(move);
        }
    }
}
