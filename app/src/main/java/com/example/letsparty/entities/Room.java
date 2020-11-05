package com.example.letsparty.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public Player getHost(){
        return this.host;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomCode.equals(room.roomCode) &&
                host.equals(room.host) &&
                players.equals(room.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomCode, host, players);
    }
}
