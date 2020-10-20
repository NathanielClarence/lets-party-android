package com.example.letsparty.entities;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void changePlayerNickname(Player player, String nickname){
        this.players.stream()
                .filter(p -> p.equals(player))
                .findFirst()
                .ifPresent(p -> p.setNickname(nickname));
    }
}
