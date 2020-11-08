package com.example.letsparty.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public void setHost(Player h){
        this.host = h;
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

    public void removePlayerByNickname(String playerName){
        this.players.removeIf(p -> p.getNickname().equals(playerName));
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

    public static Room processRoomString(String roomJSONString, String roomCode, Player player) throws JSONException {
        //process the json from server
        JSONObject jsonRoom = new JSONObject(roomJSONString);
        JSONArray jsonUsers = jsonRoom.getJSONArray("users");
        String hostname = jsonRoom.optString("host");
        List<String> usernames = IntStream.range(0, jsonUsers.length())
                .mapToObj(jsonUsers::optString)
                .collect(Collectors.toList());

        //create the host and the new room
        //just using dummy ids and tokens is fine because they're not used for anything
        Player host = new Player("host", hostname, "dummy_token");
        Room room = new Room(roomCode, host);
        //add all the other players
        usernames.stream().filter(name -> !name.equals(hostname))
                .map(name -> new Player("dummy_id", name, "dummy_token"))
                .forEach(room::addPlayer);
        //add the player
        room.addPlayer(player);

        return room;
    }
}
