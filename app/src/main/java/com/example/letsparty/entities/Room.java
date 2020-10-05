package com.example.letsparty.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private String roomCode;
    private Player host;
    private List<Player> players;

    public Room(String roomCode, Player host){
        this.roomCode = roomCode;
        this.host = host;
        this.players = new ArrayList<>();
        this.players.add(host);
    }

    public String getRoomCode(){
        return roomCode;
    }

    public List<Player> getPlayers(){
        return this.players;
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public void removePlayer(Player player){
        this.players.remove(player);
    }
}
